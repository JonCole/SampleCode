using System;
using System.Net.NetworkInformation;
using System.Threading.Tasks;

namespace BandwidthMonitor
{
    class BandwidthLogger : IDisposable
    {
        private TimeSpan _logFrequency;
        private bool _disposed;
        private long _previousReadBytes;
        private long _previousWriteBytes;
        DateTimeOffset _previousComputeTime;

        public BandwidthLogger(TimeSpan logFrequency)
        {
            if (logFrequency <= TimeSpan.Zero)
            {
                throw new ArgumentOutOfRangeException("logFrequency");
            }

            _logFrequency = logFrequency;
            _previousComputeTime = DateTimeOffset.UtcNow;
            GetNetworkUsage(out _previousReadBytes, out _previousWriteBytes);
            StartLogging();
        }

        private async void StartLogging()
        {
            try
            {
                const long bitsPerByte = 8;
                const double oneMeg = 1024 * 1024;


                while (!_disposed)
                {
                    await Task.Delay(_logFrequency);

                    long bytesRead;
                    long bytesWrite;
                    GetNetworkUsage(out bytesRead, out bytesWrite);

                    DateTimeOffset currentTime = DateTimeOffset.UtcNow;
                    TimeSpan elapsed = currentTime - _previousComputeTime;

                    long readDelta = (bytesRead - _previousReadBytes);
                    long writeDelta = (bytesWrite - _previousWriteBytes);

                    _previousReadBytes = bytesRead;
                    _previousWriteBytes = bytesWrite;
                    _previousComputeTime = currentTime;

                    double mbitsReadPerSecond = readDelta <= 0 ? 0 : ((readDelta * bitsPerByte) / oneMeg) / elapsed.TotalSeconds;
                    double mbitsWritePerSecond = writeDelta <= 0 ? 0 : ((writeDelta * bitsPerByte) / oneMeg) / elapsed.TotalSeconds;

                    LogUsage(mbitsReadPerSecond, mbitsWritePerSecond);
                }
            }
            catch (Exception)
            {

            }
        }

        protected virtual void LogUsage(double mbitsReadPerSecond, double mbitsWritePerSecond)
        {
            Console.WriteLine("[{0}] BandWidth Usage ==> READ: {1} MBits/Sec, WRITE: {2} MBits/Sec",
                DateTimeOffset.UtcNow.ToString("u"),
                Math.Round(mbitsReadPerSecond, 2),
                Math.Round(mbitsWritePerSecond, 2)
                );
        }

        private static void GetNetworkUsage(out long bytesRead, out long bytesWrite)
        {
            bytesRead = 0L;
            bytesWrite = 0L;
            try
            {
                var nics = NetworkInterface.GetAllNetworkInterfaces();

                foreach (var nic in nics)
                {
                    long nicbytesRead = nic.GetIPStatistics().BytesReceived;
                    long nicbytesWrite = nic.GetIPStatistics().BytesSent;
                    bytesRead += nicbytesRead;
                    bytesWrite += nicbytesWrite;
                }
            }
            catch (Exception)
            {

            }
        }

        public void Dispose()
        {
            _disposed = true;
        }
    }
}
