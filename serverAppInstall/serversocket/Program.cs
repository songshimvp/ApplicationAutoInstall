using System;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.IO;
using System.Drawing;
using WindowsInstaller;
using System.Diagnostics;
using System.Windows.Forms;
using System.Net.NetworkInformation;
using System.Runtime.InteropServices;

namespace serverAppInstall
{
    static class Program
    {
        private static byte[] result = new byte[4096];
        private static int myPort = 8888;
        static Socket serverSocket;
        public static string path = Environment.CurrentDirectory;

        private static string applicationLibraryPath = "E:\\Users\\MVP\\Desktop\\ApplicationLibrary\\MSI";     //服务器软件库路径

        private static String sendFilenamesStr = "";

        private static byte[] byteIcon = new byte[2048];
        private static List<byte> byteIconsList = new List<byte>();
        private static string iconsLenStr = "";

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [STAThread]
        static void Main()
        {
            if (PortInUse(8888))
            {
                MessageBox.Show("AppServer已经打开或者8888端口被占用");
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
                    Application.DoEvents(); //用Application.DoEvents()来捕获消息事件处理，但是要用死循环来控制
                }

            }
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
                String str3 = System.Text.Encoding.UTF8.GetString(result, 0, receiveNumber);
                Console.WriteLine(str3);

                //初始化
                if (byteIconsList.Count != 0)
                {
                    byteIconsList.Clear();
                }
                iconsLenStr = "";
                sendFilenamesStr = "";

                try
                {
                    Console.WriteLine("接受客户端{0}消息：{1}", myClientSocket.RemoteEndPoint.ToString(), Encoding.ASCII.GetString(result, 0, receiveNumber));

                    String[] filenames = Directory.GetFiles(applicationLibraryPath);

                    for (int i = 0; i < filenames.Length; i++)
                    {
                        string tmpFilenames = "";
                        tmpFilenames = filenames[i].Substring(applicationLibraryPath.Length + 1);       //文件名字

                        String tmpFileSizeTime = getMsiFileSizeTime(filenames[i]);

                        sendFilenamesStr += tmpFilenames + "!!!" + tmpFileSizeTime;
                        sendFilenamesStr += ",";      //每个安装文件绝对路径以“，”分割！

                        Icon icon = System.Drawing.Icon.ExtractAssociatedIcon(filenames[i]);
                        if (icon != null)
                        {
                            Bitmap bit = icon.ToBitmap();
                            //bit.Save("E:\\Users\\MVP\\Desktop\\ApplicationLibrary\\Icon\\"+i+".png", System.Drawing.Imaging.ImageFormat.Png);
                            ImageConverter converter = new ImageConverter();
                            byteIcon = (byte[])converter.ConvertTo(bit, typeof(byte[]));   //转化

                            string iconByteLen = "";
                            iconByteLen = (byteIcon.Length).ToString();
                            iconsLenStr += iconByteLen + ",";   //每个安装文件的图片“长度”以“，”分割！

                            byteIconsList.AddRange(byteIcon);
                        }
                    }
                    iconsLenStr = iconsLenStr.TrimEnd(',');
                    sendFilenamesStr = sendFilenamesStr.TrimEnd(',');

                    //Console.WriteLine(iconsLenStr);
                    Console.WriteLine(sendFilenamesStr);
                    //Console.WriteLine(byteIconsList.ToArray());
                }
                catch (System.ComponentModel.Win32Exception)
                {

                }

                if (str3 == "appInstallIni\n")   //约定“appInstallIni”
                {
                    //发送安装文件图片的"长度"和安装文件名
                    myClientSocket.Send(Encoding.UTF8.GetBytes(iconsLenStr + ":::" + sendFilenamesStr + "\n"));  //安装文件图片的长度和安装文件名以":::"分割

                    iconsLenStr = "";
                    sendFilenamesStr = "";
                }

                //发送图片
                if (str3 == "image\n")
                {
                    myClientSocket.Send(byteIconsList.ToArray());

                    //重置
                    byteIconsList.Clear();
                }

            }
            catch (Exception e)
            {
                myClientSocket.Send(Encoding.UTF8.GetBytes("error\n"));
                Console.WriteLine("异常输出:" + e.ToString());
                //myClientSocket.Close();
            }
            finally
            {
                myClientSocket.Close();
            }
        }

        //获取MSI文件的大小、最后写入时间
        private static String getMsiFileSizeTime(String msiPath)
        {
            FileInfo fi = new FileInfo(msiPath);
            Double tmp = Convert.ToDouble(fi.Length) / (1024 * 1024);
            String strSize = Convert.ToDouble(tmp).ToString("0.00");
            String strTime = fi.LastWriteTime.ToString("yyyy/MM/dd");

            //Console.WriteLine(strSize + "!!!" + strTime);

            return strSize + "!!!" + strTime;
        }

        //获取MSI文件的版本号
        private static String getMsiFileVersion(String msiPath)
        {
            System.Type oType = System.Type.GetTypeFromProgID("WindowsInstaller.Installer");
            Installer inst = System.Activator.CreateInstance(oType) as Installer;
            Database DB = inst.OpenDatabase(msiPath, MsiOpenDatabaseMode.msiOpenDatabaseModeReadOnly);
            string str = "SELECT * FROM Property WHERE Property = 'ProductVersion'";    //读取文件版本号

            string ProductVersion = "";
            if (str != "")
            {
                WindowsInstaller.View thisView = DB.OpenView(str);
                thisView.Execute();
                WindowsInstaller.Record thisRecord = thisView.Fetch();
                ProductVersion = thisRecord.get_StringData(2);       //获取特定的数据列
            }

            Console.WriteLine("文件版本号：" + ProductVersion);

            //获取失败
            //FileVersionInfo fvi = FileVersionInfo.GetVersionInfo("E:\\Users\\MVP\\Desktop\\ApplicationLibrary\\MSI\\腾讯QQ.msi");
            //Console.WriteLine("文件版本号：" + fvi.FileVersion);

            return ProductVersion;
        }

        //检测端口是否被占用
        public static bool PortInUse(int port)
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

    }

    class ConsoleWin32Helper
    {
        static ConsoleWin32Helper()
        {
            _NotifyIcon.Icon = SetIcon(Program.path + "\\ooopic_1465783183.ico");
            _NotifyIcon.Visible = false;
            _NotifyIcon.Text = "AppServer";

            ContextMenu menu = new ContextMenu();

            MenuItem item = new MenuItem();
            item.Text = "退出";
            item.Index = 0;
            item.Click += new System.EventHandler(exit_Click);
            menu.MenuItems.Add(item);

            _NotifyIcon.ContextMenu = menu;
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
            _NotifyIcon.ShowBalloonTip(500, "", "AppServer已最小化", ToolTipIcon.None);
        }
        public static void HideNotifyIcon()
        {
            _NotifyIcon.Visible = false;
        }
        #endregion
    }
} 