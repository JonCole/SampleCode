using System;
using System.Reflection;

namespace ThreadPoolMonitor
{
    class Program
    {
        static void Main(string[] args)
        {
            using (var logger = new ThreadPoolLogger(TimeSpan.FromSeconds(2)))
            {
                Console.WriteLine("Monitoring ThreadPool statistics for {0}.exe", Assembly.GetExecutingAssembly().GetName().Name);
                Console.WriteLine("Press Enter to close application");
                Console.ReadLine();
            }
        }        
    }
}
