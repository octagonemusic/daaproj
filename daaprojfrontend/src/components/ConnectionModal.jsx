import React, { useState, useEffect } from "react";
import { Modal, Button, Form } from "react-bootstrap";
import axios from "axios";

function ConnectionModal({ show, onHide, refreshData }) {
  const [connectionData, setConnectionData] = useState({
    sourceId: "",
    zoneId: "",
    costPerUnit: 0,
  });
  const [sources, setSources] = useState([]);
  const [zones, setZones] = useState([]);

  useEffect(() => {
    const fetchSourcesAndZones = async () => {
      try {
        const sourcesResponse = await axios.get(
          "http://localhost:8080/api/sources"
        );
        const zonesResponse = await axios.get(
          "http://localhost:8080/api/zones"
        );

        // Accessing the sources and zones from the response
        setSources(sourcesResponse.data._embedded.sources);
        setZones(zonesResponse.data._embedded.zones);

        console.log("Fetched Sources:", sourcesResponse.data._embedded.sources);
        console.log("Fetched Zones:", zonesResponse.data._embedded.zones);
      } catch (error) {
        console.error("Error fetching sources or zones:", error);
      }
    };

    if (show) {
      fetchSourcesAndZones();
    }
  }, [show]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Construct the full URLs for source and zone
    const connectionDataToSend = {
      source: `http://localhost:8080/api/sources/${connectionData.sourceId}`,
      zone: `http://localhost:8080/api/zones/${connectionData.zoneId}`,
      costPerUnit: connectionData.costPerUnit,
    };

    try {
      await axios.post(
        "http://localhost:8080/api/connections",
        connectionDataToSend
      );
      refreshData();
      onHide();
    } catch (error) {
      console.error("Error creating connection:", error);
    }
  };

  return (
    <Modal show={show} onHide={onHide}>
      <Modal.Header closeButton>
        <Modal.Title>Add Connection</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="sourceSelect">
            <Form.Label>Select Source</Form.Label>
            <Form.Control
              as="select"
              value={connectionData.sourceId}
              onChange={(e) =>
                setConnectionData({
                  ...connectionData,
                  sourceId: e.target.value,
                })
              }
              required
            >
              <option value="">Select Source</option>
              {sources.length > 0 ? (
                sources.map((source) => (
                  <option
                    key={source._links.source.href}
                    value={source._links.source.href.split("/").pop()}
                  >
                    Source {source._links.source.href.split("/").pop()}{" "}
                    {/* Display ID with prefix */}
                  </option>
                ))
              ) : (
                <option disabled>No sources available</option>
              )}
            </Form.Control>
          </Form.Group>
          <Form.Group controlId="zoneSelect">
            <Form.Label>Select Zone</Form.Label>
            <Form.Control
              as="select"
              value={connectionData.zoneId}
              onChange={(e) =>
                setConnectionData({ ...connectionData, zoneId: e.target.value })
              }
              required
            >
              <option value="">Select Zone</option>
              {zones.length > 0 ? (
                zones.map((zone) => (
                  <option
                    key={zone._links.zone.href}
                    value={zone._links.zone.href.split("/").pop()}
                  >
                    Zone {zone._links.zone.href.split("/").pop()}{" "}
                    {/* Display ID with prefix */}
                  </option>
                ))
              ) : (
                <option disabled>No zones available</option>
              )}
            </Form.Control>
          </Form.Group>
          <Form.Group controlId="costPerUnit">
            <Form.Label>Cost Per Unit</Form.Label>
            <Form.Control
              type="number"
              value={connectionData.costPerUnit}
              onChange={(e) =>
                setConnectionData({
                  ...connectionData,
                  costPerUnit: parseFloat(e.target.value),
                })
              }
              required
            />
          </Form.Group>
          <Button variant="primary" type="submit" className="mt-3">
            Save Connection
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
}

export default ConnectionModal;
