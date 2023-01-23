

var UploadMapView = Backbone.View.extend({

	initialize: function ( opts ) {

      var stopHT = `
		<strong>Parada: {{stopSequence}}</strong><br/>
		Subidas: {{board}}<br/>
		Bajadas: {{alight}}<br/>
		Tiemo de recorrido: {{minutes}}:{{remainder}}
      `;
      var patternDetailsHT = `
		<strong>Descripción:</strong> {{routeDescription}}<br/>
		<strong>Notas:</strong> {{routeNotes}}<br/>
      `;


      var view = this;
      this.unitId = opts.unitId;
      //this.stopTemplate = Handlebars.compile( $("#stop-popup-tpl").html());
      this.stopTemplate = Handlebars.compile(stopHT);
      //this.patternDetailsTemplate = Handlebars.compile( $("#pattern-details-tpl").html());
      this.patternDetailsTemplate = Handlebars.compile(patternDetailsHT);

      this.stopIcon = L.icon({
          iconUrl: 'images/icons/stop_icon.png',
          iconSize: [32, 37],
          iconAnchor: [16, 37],
          popupAnchor: [0, -37],
          labelAnchor: [10, -16]
        });
    

    
     // Base layer config is optional, default to Mapbox Streets
      var url = 'https://api.mapbox.com/styles/v1/mapbox/streets-v10/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1IjoicmRyb3VhaWxsZXQiLCJhIjoicVpsbVBJMCJ9.h4fZCzXIRtKQpapfS9B9xg';
          baseLayer = L.tileLayer(url, {
            attribution: '&copy; OpenStreetMap contributors, CC-BY-SA. <a href="http://mapbox.com/about/maps" target="_blank">Terms &amp; Feedback</a>' 
          });

      // Init the map
      this.map = L.map($('#map').get(0), {
        center: defaultLatLon, //TODO: add to the config file for now
        zoom: 15,
        maxZoom: 17
      });

      this.map.addLayer(baseLayer);

      this.stopLayer = L.layerGroup().addTo(this.map);

      // Remove default prefix
      this.map.attributionControl.setPrefix('');

      this.map.on('contextmenu', this.addIncident, this);

    },

    update: function(data) {
        
        if(this.polyline != undefined)
          this.map.removeLayer(this.polyline);

        $('#patternDetails').html(this.patternDetailsTemplate(data));
        
        this.polyline = L.Polyline.fromEncoded(data.shape).addTo(this.map);
        this.map.fitBounds(this.polyline.getBounds());

        this.stopLayer.clearLayers();
        for(var s in data.stops) {
          var marker = L.marker([data.stops[s].lat, data.stops[s].lon]);
  
          var remainder = data.stops[s].travelTime % 60;
          var stopData = {lat: data.stops[s].lat, lon: data.stops[s].lon, remainder: remainder, minutes: (data.stops[s].travelTime - remainder) / 60, stopSequence:  parseInt(s) + 1, board: data.stops[s].board, alight: data.stops[s].alight};

          marker.bindPopup(this.stopTemplate(stopData));
          this.stopLayer.addLayer(marker);

        }

    }

   
 });  