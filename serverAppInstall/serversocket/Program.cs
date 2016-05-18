using System;
using System.Collections.Generic;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.IO;
using System.Drawing;

namespace serverAppInstall
{
    static class Program
    {
        private static byte[] result = new byte[4096];
        private static int myPort = 8888;
        static Socket serverSocket;

        private static string applicationLibraryPath = "E:\\Users\\MVP\\Desktop\\ApplicationLibrary\\EXE";     //服务器软件库路径

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
                String str3 = System.Text.Encoding.UTF8.GetString(result, 0, receiveNumber);
                Console.WriteLine(str3);

                //初始化
                if(byteIconsList.Count!=0)
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
                        tmpFilenames = filenames[i].Substring(applicationLibraryPath.Length+1);
                        sendFilenamesStr += tmpFilenames;
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
    }
} 