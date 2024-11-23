import React, { useState, useEffect } from "react";
import axios from "axios";
import ZoneModal from "./components/ZoneModal";
import SourceModal from "./components/SourceModal";
import ConnectionModal from "./components/ConnectionModal";
import WaterAllocationSystem from "./components/WaterAllocationSystem";
import "bootstrap/dist/css/bootstrap.min.css";

function App() {
  const [zones, setZones] = useState([]);
  const [sources, setSources] = useState([]);
  const [connections, setConnections] = useState([]);
  const [showZoneModal, setShowZoneModal] = useState(false);
  const [showSourceModal, setShowSourceModal] = useState(false);
  const [showConnectionModal, setShowConnectionModal] = useState(false);

  const fetchZones = async () => {
    const response = await axios.get("http://localhost:8080/api/zones");
    setZones(response.data._embedded.zones || []);
  };

  const fetchSources = async () => {
    const response = await axios.get("http://localhost:8080/api/sources");
    setSources(response.data._embedded.sources || []);
  };

  const fetchConnections = async () => {
    const response = await axios.get("http://localhost:8080/api/connections");
    setConnections(response.data._embedded.connections || []);
  };

  useEffect(() => {
    fetchZones();
    fetchSources();
    fetchConnections();
  }, []);

  return (
    <div className="container">
      <h1>Water Allocation System</h1>
      <WaterAllocationSystem />
      <button
        className="btn btn-primary my-2"
        onClick={() => setShowZoneModal(true)}
      >
        Add Zone
      </button>
      <button
        className="btn btn-primary my-2"
        onClick={() => setShowSourceModal(true)}
      >
        Add Source
      </button>
      <button
        className="btn btn-primary my-2"
        onClick={() => setShowConnectionModal(true)}
      >
        Add Connection
      </button>

      <ZoneModal
        show={showZoneModal}
        onHide={() => setShowZoneModal(false)}
        refreshData={fetchZones}
      />
      <SourceModal
        show={showSourceModal}
        onHide={() => setShowSourceModal(false)}
        refreshData={fetchSources}
      />
      <ConnectionModal
        show={showConnectionModal}
        onHide={() => setShowConnectionModal(false)}
        zones={zones}
        sources={sources}
        refreshData={fetchConnections}
      />
    </div>
  );
}

export default App;
