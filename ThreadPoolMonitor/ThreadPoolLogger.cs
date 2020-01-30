using System;
using System.Threading;
using System.Threading.Tasks;

namespace ThreadPoolMonitor
{
	class ThreadPoolLogger : IDisposable
	{
		private TimeSpan _logFrequency;
		private bool _disposed;

		public ThreadPoolLogger(TimeSpan logFrequency)
		{
			if (logFrequency <= TimeSpan.Zero)
			{
				throw new ArgumentOutOfRangeException("logFrequency");
			}

			_logFrequency = logFrequency;
			StartLogging();
		}

		private async void StartLogging()
		{
			try
			{
				while (!_disposed)
				{
					await Task.Delay(_logFrequency);

					var stats = GetThreadPoolStats();

					LogUsage(stats);
				}
			}
			catch (Exception)
			{

			}
		}

		protected virtual void LogUsage(ThreadPoolUsageStats stats)
		{
			string message = string.Format("[{0}] IOCP:(Busy={1},Min={2},Max={3}), WORKER:(Busy={4},Min={5},Max={6}), Local CPU: {7}",
				DateTimeOffset.UtcNow.ToString("u"),
				stats.BusyIoThreads, stats.MinIoThreads, stats.MaxIoThreads,
				stats.BusyWorkerThreads, stats.MinWorkerThreads, stats.MaxWorkerThreads,
				PerfCounterHelper.GetSystemCPU()
				);

			Console.WriteLine(message);
		}

		/// <summary>
		/// Returns the current thread pool usage statistics for the CURRENT AppDomain/Process
		/// </summary>
		public static ThreadPoolUsageStats GetThreadPoolStats()
		{
			//BusyThreads =  TP.GetMaxThreads() –TP.GetAVailable();
			//If BusyThreads >= TP.GetMinThreads(), then threadpool growth throttling is possible.

			int maxIoThreads, maxWorkerThreads;
			ThreadPool.GetMaxThreads(out maxWorkerThreads, out maxIoThreads);

			int freeIoThreads, freeWorkerThreads;
			ThreadPool.GetAvailableThreads(out freeWorkerThreads, out freeIoThreads);

			int minIoThreads, minWorkerThreads;
			ThreadPool.GetMinThreads(out minWorkerThreads, out minIoThreads);

			int busyIoThreads = maxIoThreads - freeIoThreads;
			int busyWorkerThreads = maxWorkerThreads - freeWorkerThreads;

			return new ThreadPoolUsageStats
			{
				BusyIoThreads = busyIoThreads,
				MinIoThreads = minIoThreads,
				MaxIoThreads = maxIoThreads,
				BusyWorkerThreads = busyWorkerThreads,
				MinWorkerThreads = minWorkerThreads,
				MaxWorkerThreads = maxWorkerThreads,
			};
		}

		public void Dispose()
		{
			_disposed = true;
		}
	}

	public struct ThreadPoolUsageStats
	{
		public int BusyIoThreads { get; set; }

		public int MinIoThreads { get; set; }

		public int MaxIoThreads { get; set; }

		public int BusyWorkerThreads { get; set; }

		public int MinWorkerThreads { get; set; }

		public int MaxWorkerThreads { get; set; }
	}
}
