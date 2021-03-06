﻿using System;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.IO;
using Microsoft.Win32;
using System.Windows.Forms;
using System.Drawing;
using System.Net.NetworkInformation;

namespace AppAutoInstall
{
    static class AutoInstall
    {
        private static int myPort = 12123;   //端口号
        private static Socket serverSocket;
        public static string path = Environment.CurrentDirectory;

        private static byte[] result = new byte[4096];   //接收
        private static String receiveAppnamesStrs;       //用户要求安装的Application字符串
        private static String[] appNameStr;              //用户要求安装的Application

        private static String failedApp;
        private static String successApp;
        private static String failedSuccessApp;

        private static int AppDownloadFlag;
        private static int AppInstallFlag;
        private static int TimeOutFlag;

        private static String AppDownloadCMDPath = path + "\\APPDownload.cmd";    //FTP自动下载脚本文件绝对路径
        private static String AppInstallCMDPath = path + "\\APPInstall.cmd";      //自动安装脚本文件绝对路径

        public enum ShowCommands : int
        {
            SW_HIDE = 0,
            SW_SHOWNORMAL = 1,
            SW_NORMAL = 1,
            SW_SHOWMINIMIZED = 2,
            SW_SHOWMAXIMIZED = 3,
            SW_MAXIMIZE = 3,
            SW_SHOWNOACTIVATE = 4,
            SW_SHOW = 5,
            SW_MINIMIZE = 6,
            SW_SHOWMINNOACTIVE = 7,
            SW_SHOWNA = 8,
            SW_RESTORE = 9,
            SW_SHOWDEFAULT = 10,
            SW_FORCEMINIMIZE = 11,
            SW_MAX = 11
        }
        [DllImport("shell32.dll")]
        static extern IntPtr ShellExecute(
            IntPtr hwnd,
            string lpOperation,
            string lpFile,
            string lpParameters,
            string lpDirectory,
            ShowCommands nShowCmd);
        //int ExecuteN = (int)ShellExecute(IntPtr.Zero, "open", "E:\\Users\\MVP\\Desktop\\E2Mmsi\\" + app, null, null, ShowCommands.SW_SHOWNORMAL);   //(LPCWSTR)L解决参数类型不兼容

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [STAThread]
        static void Main()
        {
            if (PortInUse(12123))
            {
                MessageBox.Show("AppAutoInstall已经打开或者12123端口被占用");
            }
            else
            {
                ConsoleWin32Helper.ShowNotifyIcon();  //显示系统托盘                

                IPAddress ip = IPAddress.Parse("0.0.0.0");
                serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                serverSocket.Bind(new IPEndPoint(ip, myPort));
                serverSocket.Listen(10);
                Console.WriteLine("启动监听{0}成功", serverSocket.LocalEndPoint.ToString());
                Thread myThread = new Thread(ListenClientConnect);
                myThread.Start();

                while (true)
                {
                    Application.DoEvents();  //用Application.DoEvents()来捕获消息事件处理，但是要用死循环来控制
                }
            }
        }      

        private static void ListenClientConnect()
        {
            while (true)
            {
                Socket clientSocket = serverSocket.Accept();
                Thread receiveThread = new Thread(ReceiveMessage);
                Thread.Sleep(2000);
                receiveThread.Start(clientSocket);
            }
        }

        private static void ReceiveMessage(object clientSocket)
        {
            Socket myClientSocket = (Socket)clientSocket;
            try
            {
                int receiveNumber = myClientSocket.Receive(result);
                Console.WriteLine("启动监听{0}成功", myClientSocket.RemoteEndPoint.ToString());

                receiveAppnamesStrs = System.Text.Encoding.UTF8.GetString(result, 0, receiveNumber);
                Console.WriteLine("请求安装：{0}", receiveAppnamesStrs);

                //安装用户要求的应用
                try
                {
                    if (receiveAppnamesStrs != null)
                    {
                        installRequiredApp(receiveAppnamesStrs, myClientSocket);
                    }
                }
                catch (Exception e)
                {
                    myClientSocket.Send(Encoding.UTF8.GetBytes("error!!!error1\n"));
                    Console.WriteLine("安装异常+:{0}", e.Message);
                    ConsoleWin32Helper.HideNotifyIcon();
                    System.Environment.Exit(0);
                }
            }
            catch (Exception e)
            {
                myClientSocket.Send(Encoding.UTF8.GetBytes("error!!!error2\n"));
                Console.WriteLine("接收确认、安装异常++:{0}", e.Message);
                ConsoleWin32Helper.HideNotifyIcon();
                System.Environment.Exit(0);
            }
            finally
            {
                try
                {
                    if (myClientSocket != null)
                    {
                        myClientSocket.Close();
                    }
                        
                }
                catch (Exception e)
                {
                    Console.WriteLine("socket异常{0}", e.Message);
                    ConsoleWin32Helper.HideNotifyIcon();
                    System.Environment.Exit(0);
                }
            }
        }

        private static void installRequiredApp(String receiveAppnames, Socket myClientSocket)
        {
            appNameStr = receiveAppnames.Split(',');

            int appCount = appNameStr.Length;
            Console.WriteLine("总共" + (appCount-1) + "个应用");

            for (int i = 0; i < appCount-1; i++)
            {
                Stopwatch watchTimer = new Stopwatch();
                watchTimer.Start();
                Thread watchThread = new Thread(watchTimeFun);
                watchThread.Start(watchTimer);          //开启线程、同步计时

                //初始化字符串
                failedApp = "";
                successApp = "";
                failedSuccessApp = "";

                //检测是否安装成功标志位
                AppDownloadFlag = -1;
                AppInstallFlag = -1;
                TimeOutFlag = -1;

                //一个应用一个应用的启动下载、安装
                string app = appNameStr[i] + ".msi";
                //ProcessStartInfo infoStartCMD = new ProcessStartInfo();
                if (app != "\n.msi")   //传过来的字符串最后有个“\n”
                {
                    //private static String AppDownloadCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPDownload.cmd";    //FTP自动下载脚本文件绝对路径
                    //private static String AppInstallCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPInstall.cmd";      //自动安装脚本文件绝对路径

                    //自动批量下载安装文件（FTP）
                    ExeCommand(AppDownloadCMDPath, "\"" + app + "\"");     //防止所要下载文件的文件名中有空格

                    String appFilePath = path + "\\" + app;
                    FileInfo appFile = new FileInfo(appFilePath);
                    if (appFile.Exists)
                    {
                        AppDownloadFlag = 0;      //下载成功

                        if(appNameStr[i] == "腾讯QQ")
                        {
                            bool closeFlag = true;
                            Process[] process = Process.GetProcesses();
                            for (int p = 0; closeFlag && p < process.Length; p++)
                            {
                                if (process[p].ProcessName == "QQ")
                                {
                                    process[p].Kill();
                                    closeFlag = false;
                                }
                            }
                        }
                        else if (appNameStr[i] == "WPS Office")
                        {
                            Process[] process = Process.GetProcesses();
                            for (int p = 0; p < process.Length; p++)
                            {
                                switch(process[p].ProcessName)
                                {
                                    case "wps":    //word
                                    case "et":     //excel
                                    case "wpp":    //ppt
                                        process[p].Kill();
                                        break;
                                    default:
                                        break;
                                }
                                
                            }
                        }

                        //自动安装
                        ExeCommand(AppInstallCMDPath, app);

                        if (watchTimer.IsRunning && watchTimer.ElapsedMilliseconds < 5 * 60 * 1000)
                        {
                            Console.WriteLine("耗时：" + watchTimer.ElapsedMilliseconds);
                            TimeOutFlag = 0;         //未超时
                            try
                            {
                                watchThread.Abort();
                            }
                            catch (ThreadAbortException) { }

                            watchTimer.Stop();
                            watchTimer.Reset();

                            if (checkAPPInWindows(appNameStr[i]))
                            {
                                AppInstallFlag = 0;      //注册表检测到，安装成功
                            }
                        }

                        else
                        {
                            //终止安装程序
                        }
                    }

                    if (AppDownloadFlag == 0 && AppInstallFlag == 0 && TimeOutFlag == 0)
                    {
                        successApp += appNameStr[i] + ",";
                    }
                    else
                    {
                        failedApp += appNameStr[i] + ",";
                    }
                
                }

                if (failedApp != "")
                {
                    failedSuccessApp += failedApp;
                }
                else
                {
                    failedSuccessApp += "failed";
                }

                if (successApp != "")
                {
                    failedSuccessApp += "!!!" + successApp;
                }
                else
                {
                    failedSuccessApp += "!!!" + "success";
                }
                Console.WriteLine("安装失败或成功:{0}", failedSuccessApp);

                myClientSocket.Send(Encoding.UTF8.GetBytes(failedSuccessApp + "\n"));

                Thread.Sleep(2000);       //等待两秒再继续，如果紧接的两个应用都是安装失败，Android端接收的数据将会错误
            }//for END!
        }

        //启动相应的脚本文件
        public static void ExeCommand(String path, String app)
        {
            Process p = new Process();
            p.StartInfo.FileName = path;         //这种方式不能直接启动msi文件
            p.StartInfo.Arguments = app;
            p.StartInfo.UseShellExecute = false;
            p.StartInfo.RedirectStandardInput = true;
            p.StartInfo.RedirectStandardOutput = true;
            p.StartInfo.RedirectStandardError = true;
            p.StartInfo.CreateNoWindow = true;
            //设置启动动作,确保以管理员身份运行
            p.StartInfo.Verb = "runas";
            try
            {
                p.Start();

                p.WaitForExit();     //可设置等待时间

                if (p != null)
                {
                    p.Close();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("启动CMD文件出错：{0}", e.Message);
            }
        }

        //检测软件是否安装
        private static bool checkAPPInWindows(String app)
        {
            //定义注册表操作类并指向注册表的软件信息目录
            Microsoft.Win32.RegistryKey uninstallNode = Microsoft.Win32.Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall");

            foreach(string subKeyName in uninstallNode.GetSubKeyNames())
            {
                Microsoft.Win32.RegistryKey subKey = uninstallNode.OpenSubKey(subKeyName);   //定义注册表搜索子类
                object displayName = subKey.GetValue("DisplayName");     //搜索键名为DisplayName的键————根据软件名字查找

                //针对特定软件，检查“InstallRoot”
                //RegistryKey akeytwo = rk.OpenSubKey(@"SOFTWARE\Kingsoft\Office\6.0\common\");  //查询wps——excel表格               
                //string filewps = akeytwo.GetValue("InstallRoot").ToString();
                //if (File.Exists(filewps + @"\office6\et.exe"))

                if (displayName != null)
                {
                    if (displayName.ToString().Contains(app))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        //对于任何一款应用，从下载到安装成功不得超过5分钟，否则将强制关闭
        private static void watchTimeFun(object tmp)
        {
            Stopwatch watchTimer = (Stopwatch)tmp;
            bool flag = true;
            while (flag)
            {
                if (watchTimer.ElapsedMilliseconds >= 5 * 60 * 1000)
                {
                    Process[] process = Process.GetProcesses();
                    for (int p = 0; p < process.Length; p++)
                    {
                        switch (process[p].MainWindowTitle)
                        {
                            case "Exe to msi converter free":
                            case "WPS Office":
                                process[p].Kill();
                                break;
                            default:
                                break;
                        }
                    }

                    //TimeOutFlag = -1;       //超时
                    flag = false;

                    watchTimer.Stop();
                    watchTimer.Reset();
                }
            }
        }

        //检测端口是否被占用
        private static bool PortInUse(int port)
        {
            bool inUse = false;

            IPGlobalProperties ipProperties = IPGlobalProperties.GetIPGlobalProperties();
            IPEndPoint[] ipEndPoints = ipProperties.GetActiveTcpListeners();

            foreach (IPEndPoint endPoint in ipEndPoints)
            {
                if (endPoint.Port == port)
                {
                    inUse = true;
                    break;
                }
            }
            return inUse;
        }

        //从FTP服务器上下载文件
        private static int DownloadAppByFtp(string filename)
        {
            FtpWebRequest reqFTP;
            string serverIP;
            string userName;
            string password;
            string url;

            try
            {
                serverIP = System.Configuration.ConfigurationManager.AppSettings["192.168.1.116"];
                userName = System.Configuration.ConfigurationManager.AppSettings["MVP"];
                password = System.Configuration.ConfigurationManager.AppSettings["*****"];
                url = "ftp://" + serverIP + "/" + Path.GetFileName(filename);

                FileStream outputStream = new FileStream("E:\\Users\\MVP\\Desktop\\E2Mmsi\\" + filename, FileMode.Create);    //下载的文件保存路径
                reqFTP = (FtpWebRequest)FtpWebRequest.Create(new Uri(url));
                reqFTP.Method = WebRequestMethods.Ftp.DownloadFile;
                reqFTP.UseBinary = true;
                reqFTP.KeepAlive = false;
                reqFTP.Credentials = new NetworkCredential(userName, password);
                FtpWebResponse response = (FtpWebResponse)reqFTP.GetResponse();
                Stream ftpStream = response.GetResponseStream();
                long cl = response.ContentLength;
                int bufferSize = 2048;
                int readCount;
                byte[] buffer = new byte[bufferSize];
                readCount = ftpStream.Read(buffer, 0, bufferSize);
                while (readCount > 0)
                {
                    outputStream.Write(buffer, 0, readCount);
                    readCount = ftpStream.Read(buffer, 0, bufferSize);
                }
                ftpStream.Close();
                outputStream.Close();
                response.Close();
                return 0;
            }
            catch (Exception ex)
            {
                return -2;
            }
        }

    }

    //管理系统托盘
    class ConsoleWin32Helper
    {
        static ConsoleWin32Helper()
        {
            _NotifyIcon.Icon = SetIcon(AutoInstall.path + "\\ooopic_1465784277.ico");
            _NotifyIcon.Visible = false;
            _NotifyIcon.Text = "一键远程安装";
            ContextMenu menu = new ContextMenu();
            _NotifyIcon.ContextMenu = menu;

            MenuItem item = new MenuItem();
            item.Text = "退出";
            item.Index = 0;
            item.Click += new System.EventHandler(exit_Click);
            menu.MenuItems.Add(item);            
        }

        [DllImport("shell32.DLL", EntryPoint = "ExtractAssociatedIcon")]
        private static extern int ExtractAssociatedIconA(int hInst, string lpIconPath, ref int lpiIcon); //声明函数

        static System.IntPtr thisHandle;
        private static System.Drawing.Icon SetIcon(string path)
        {
            int RefInt = 0;
            thisHandle = new IntPtr(ExtractAssociatedIconA(0, path, ref RefInt));
            return System.Drawing.Icon.FromHandle(thisHandle);
        }

        [DllImport("User32.dll", EntryPoint = "ShowWindow")]
        private static extern bool ShowWindow(IntPtr hWnd, int type);

        static void exit_Click(object sender, EventArgs e)
        {
            //Console.WriteLine("退出");
            HideNotifyIcon();
            System.Environment.Exit(0);          
        }

        #region 托盘图标
        static NotifyIcon _NotifyIcon = new NotifyIcon();
        public static void ShowNotifyIcon()
        {
            _NotifyIcon.Visible = true;
            _NotifyIcon.ShowBalloonTip(200, "", "一键远程安装服务已最小化", ToolTipIcon.None);
        }
        public static void HideNotifyIcon()
        {
            _NotifyIcon.Visible = false;
        }
        #endregion
    }
}