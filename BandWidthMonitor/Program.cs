﻿using BandwidthMonitor;
using System;

namespace BandWidthMonitor
{
    class Program
    {
        static void Main(string[] args)
        {
            using (var bandwidthLogger = new BandwidthLogger(TimeSpan.FromSeconds(2)))
            {
                Console.WriteLine("Press Enter to close application");
                Console.ReadLine();
            }
        }
    }
}
