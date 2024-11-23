import React, { useState, useEffect } from "react";
import axios from "axios";
import VisualizationArea from "./VisualizationArea";
import ControlPanel from "./ControlPanel";
import "./WaterAllocationSystem.css";

const WaterAllocationSystem = () => {
  const [sources, setSources] = useState([]);
  const [zones, setZones] = useState([]);
  const [connections, setConnections] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [isAllocating, setIsAllocating] = useState(false);

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    try {
      const [sourcesRes, zonesRes, connectionsRes] = await Promise.all([
        axios.get("http://localhost:8080/api/sources"),
        axios.get("http://localhost:8080/api/zones"),
        axios.get("http://localhost:8080/api/connections"),
      ]);
      setSources(sourcesRes.data._embedded?.sources || []);
      setZones(zonesRes.data._embedded?.zones || []);
      setConnections(connectionsRes.data._embedded?.connections || []);
    } catch (error) {
      console.error("Error fetching data:", error);
    }
  };

  const handleAllocateWater = async () => {
    setIsAllocating(true);
    try {
      // Log allocation response
      const response = await axios.get("http://localhost:8080/allocate-water");
      console.log("Allocation response:", response.data);
      setAllocations(response.data.allocations);

      // Refresh and log sources data
      const sourcesRes = await axios.get("http://localhost:8080/api/sources");
      console.log("Updated sources:", sourcesRes.data._embedded?.sources);
      setSources(sourcesRes.data._embedded?.sources || []);

      // Refresh zones data
      const zonesRes = await axios.get("http://localhost:8080/api/zones");
      setZones(zonesRes.data._embedded?.zones || []);
    } catch (error) {
      console.error("Error allocating water:", error);
    } finally {
      setIsAllocating(false);
    }
  };

  return (
    <div className="water-allocation-system">
      <ControlPanel
        onAllocate={handleAllocateWater}
        isAllocating={isAllocating}
      />
      <VisualizationArea
        sources={sources}
        zones={zones}
        connections={connections}
        allocations={allocations}
      />
    </div>
  );
};

export default WaterAllocationSystem;
