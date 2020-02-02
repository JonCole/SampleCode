using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using FlightFinder.Shared;
using Microsoft.AspNetCore.Components;

namespace FlightFinder.Client.Services
{
    public class AppState
    {
        // Actual state
        public IReadOnlyList<Itinerary> SearchResults { get; private set; }
        public bool SearchInProgress { get; private set; }

        private IList<Itinerary> shortlist = new List<Itinerary>();
        public IReadOnlyList<Itinerary> Shortlist => (IReadOnlyList<Itinerary>)shortlist;

        // Lets components receive change notifications
        // Could have whatever granularity you want (more events, hierarchy...)
        public event Action OnChange;

        // Receive 'http' instance from DI
        private readonly HttpClient http;
        public AppState(HttpClient httpInstance)
        {
            http = httpInstance;
        }

        public async Task Search(SearchCriteria criteria)
        {
            SearchInProgress = true;
            NotifyStateChanged();

            SearchResults = await http.PostJsonAsync<Itinerary[]>("/api/flightsearch", criteria);
            SearchInProgress = false;
            NotifyStateChanged();
        }

        public async Task LoadShortList()
        {
            // Haven't yet figured out how to get this to be invoked on page load...
            shortlist = (await http.GetJsonAsync<IList<Itinerary>>("/api/shortlist"));
            NotifyStateChanged();            
        }

        public async Task AddToShortlist(Itinerary itinerary)
        {
            shortlist = (await http.PostJsonAsync<IList<Itinerary>>("/api/shortlist/add", itinerary));
            NotifyStateChanged();
        }

        public async Task RemoveFromShortlist(Itinerary itinerary)
        {
            shortlist = (await http.PostJsonAsync<IList<Itinerary>>("/api/shortlist/remove", itinerary));
            NotifyStateChanged();
        }

        private void NotifyStateChanged() => OnChange?.Invoke();
    }
}
