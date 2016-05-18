using System;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.IO;

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
        private static int AppDownloadFlag = -1;
        private static int AppInstallFlag = -1;
        private static String failedSuccessApp;

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

            Thread myThread = new Thread(ListenClientConnect);
            myThread.Start();
        }

        private static void ListenClientConnect()
        {
            while (true)
            {
                Socket clientSocket = serverSocket.Accept();
                Thread receiveThread = new Thread(ReceiveMessage);
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
                Console.WriteLine("请求安装{0}", receiveAppnamesStrs);

                //myClientSocket.Send(Encoding.UTF8.GetBytes("installing!!!installing"));

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
                    Console.WriteLine("安装异常+++++:{0}", e.Message);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("接收确认、安装异常+++++:{0}", e.Message);
                myClientSocket.Send(Encoding.UTF8.GetBytes("error\n"));
            }
            finally
            {
                try
                {
                    if (myClientSocket != null)
                        myClientSocket.Close();
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

            //初始化
            failedApp = "";
            successApp = "";
            failedSuccessApp = "";

            for (int i = 0; i < appCount; i++)
            {
                //一个应用一个应用的启动下载、安装
                string app = appNameStr[i] + ".msi";
                //ProcessStartInfo infoStartCMD = new ProcessStartInfo();
                if (app != "\n.msi")   //传过来的字符串最后有个“\n”
                {
                    //private static String AppDownloadCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPDownload.cmd";    //FTP自动下载脚本文件绝对路径
                    //private static String AppInstallCMDPath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\APPInstall.cmd";      //自动安装脚本文件绝对路径

                    //自动下载安装文件（FTP）                   
                    ExeCommand(AppDownloadCMDPath,app);    
                    
                    String appFilePath = "E:\\Users\\MVP\\Desktop\\E2Mmsi\\" + app;
                    FileInfo appFile = new FileInfo(appFilePath);
                    if(appFile.Exists)
                    {
                        AppDownloadFlag = 0;      //下载成功

                        //自动安装                    
                        ExeCommand(AppInstallCMDPath, app);
                        if (checkAPPInWindows(appNameStr[i]))
                        {
                            AppInstallFlag = 0;   //安装成功
                        }
                    }

                    
                    if (AppDownloadFlag == 0 && AppInstallFlag == 0)
                    {
                        successApp += appNameStr[i] + ",";
                    }
                    else
                    {
                        failedApp += appNameStr[i] + ",";
                    }
                }
 
            }//for END!

            if (failedApp != "")
            {
                failedSuccessApp += failedApp.TrimEnd(',');
            }
            else
            {
                failedSuccessApp += "";
            }
            if(successApp != "")
            {
                failedSuccessApp += "!!!" + successApp.TrimEnd(',');
            }
            else
            {
                failedSuccessApp += "!!!" + "";
            }
            Console.WriteLine("安装失败或成功:{0}", failedSuccessApp.TrimEnd(','));

            myClientSocket.Send(Encoding.UTF8.GetBytes(failedSuccessApp.TrimEnd(',')));    
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
                serverIP = System.Configuration.ConfigurationManager.AppSettings["192.168.1.116"];
                userName = System.Configuration.ConfigurationManager.AppSettings["MVP"];
                password = System.Configuration.ConfigurationManager.AppSettings["song19911020"];
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
            
            try
            {
                p.Start();                              

                p.WaitForExit();
                
                if(p!=null)
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


    }     
}