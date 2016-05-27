using System;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.IO;
using Microsoft.Win32;

namespace AppAutoInstall
{
    static class AutoInstall
    {
        private static int myPort = 12123;   //端口号
        static Socket serverSocket;

        private static byte[] result = new byte[4096];   //接收
        private static String receiveAppnamesStrs;       //用户要求安装的Application字符串
        private static String[] appNameStr;              //用户要求安装的Application

        private static String failedApp;
        private static String successApp;
        private static String failedSuccessApp;

        private static int AppDownloadFlag;
        private static int AppInstallFlag;
        private static int TimeOutFlag;

        private static String AppDownloadCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPDownload.cmd";    //FTP自动下载脚本文件绝对路径
        private static String AppInstallCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPInstall.cmd";      //自动安装脚本文件绝对路径

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
            IPAddress ip = IPAddress.Parse("0.0.0.0");
            serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            serverSocket.Bind(new IPEndPoint(ip, myPort));
            serverSocket.Listen(10);
            Console.WriteLine("启动监听{0}成功", serverSocket.LocalEndPoint.ToString());

            //准备工作————为客户机新建本地存储文件夹
            if(Directory.Exists("E:\\Users\\MVP\\Desktop\\E2Mmsi") == false)
            {
                Directory.CreateDirectory("E:\\Users\\MVP\\Desktop\\E2Mmsi");
            }
            
            Thread myThread = new Thread(ListenClientConnect);
            myThread.Start();
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
                }
            }
            catch (Exception e)
            {
                myClientSocket.Send(Encoding.UTF8.GetBytes("error!!!error2\n"));
                Console.WriteLine("接收确认、安装异常++:{0}", e.Message);
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
                    Console.WriteLine("异常{0}", e.Message);
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
                    ExeCommand(AppDownloadCMDPath, app);

                    String appFilePath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\" + app;
                    FileInfo appFile = new FileInfo(appFilePath);
                    if (appFile.Exists)
                    {
                        AppDownloadFlag = 0;      //下载成功

                        if(appNameStr[i] == "腾讯QQ")
                        {
                            Process[] process = Process.GetProcesses();
                            for (int p = 0; p < process.Length; p++)
                            {
                                if (process[p].ProcessName == "QQ")
                                {
                                    process[p].Kill();         //安装腾讯QQ之前必须先关闭QQ
                                }
                            }
                        }

                        //自动安装
                        ExeCommand(AppInstallCMDPath, app);

                        if (watchTimer.IsRunning && watchTimer.ElapsedMilliseconds < 2 * 60 * 1000)
                        {
                            Console.WriteLine("时间：" + watchTimer.ElapsedMilliseconds);
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
                                AppInstallFlag = 0;    //安装成功
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
                Console.WriteLine("启动出错：{0}", e.Message);
            }
        }

        //检测软件是否安装
        private static bool checkAPPInWindows(String app)
        {
            //定义注册表操作类并指向注册表的软件信息目录
            Microsoft.Win32.RegistryKey uninstallNode = Microsoft.Win32.Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall");

            RegistryKey idCard = Registry.LocalMachine.OpenSubKey("SOFTWARE", true).OpenSubKey(@"SDTtelecom", true);
            idCard = Registry.LocalMachine.OpenSubKey("SOFTWARE", true).OpenSubKey(@"SDTtelecom", true);

            foreach (string subKeyName in uninstallNode.GetSubKeyNames())
            {
                Microsoft.Win32.RegistryKey subKey = uninstallNode.OpenSubKey(subKeyName);   //定义注册表搜索子类
                object displayName = subKey.GetValue("DisplayName");     //搜索键名为DisplayName的键————根据软件名字查找
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

        //对于任何一款应用，从下载到安装成功不得超过2分钟，否则将强制关闭
        private static void watchTimeFun(object tmp)
        {
            Stopwatch watchTimer = (Stopwatch)tmp;
            bool flag = true;
            while (flag)
            {
                if (watchTimer.ElapsedMilliseconds >= 2 * 60 * 1000)
                {
                    Process[] process = Process.GetProcesses();
                    for (int p = 0; p < process.Length; p++)
                    {
                        if (process[p].MainWindowTitle == "Exe to msi converter free")
                        {
                            process[p].Kill();
                        }
                    }

                    //TimeOutFlag = -1;       //超时
                    flag = false;

                    watchTimer.Stop();
                    watchTimer.Reset();
                }
            }
        }

        //从FTP服务器上下载文件
        public static int DownloadAppByFtp(string filename)
        {
            FtpWebRequest reqFTP;
            string serverIP;
            string userName;
            string password;
            string url;

            try
            {
                serverIP = System.Configuration.ConfigurationManager.AppSettings["192.168.1.***"];
                userName = System.Configuration.ConfigurationManager.AppSettings["MVP"];
                password = System.Configuration.ConfigurationManager.AppSettings["*******"];
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

        private static void newSendSocket()
        {
            while (true)
            {
                String androidIPStr = "";
                Socket sendSocket;
                int androidPort = 8885;

                IPAddress androidIP = IPAddress.Parse(androidIPStr);
                sendSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                try
                {
                    sendSocket.Connect(new IPEndPoint(androidIP, androidPort));
                }
                catch (Exception e)
                {
                    Console.WriteLine("异常{0}", e.Message);
                }
            }
        }
    }     
}