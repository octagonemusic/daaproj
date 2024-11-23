import React, { useState } from "react";
import { Modal, Button, Form } from "react-bootstrap";
import axios from "axios";

function ZoneModal({ show, onHide, refreshData }) {
  const [zoneData, setZoneData] = useState({
    demand: 0,
    priority: 1,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await axios.post("http://localhost:8080/api/zones", zoneData);
    refreshData();
    onHide();
  };

  return (
    <Modal show={show} onHide={onHide}>
      <Modal.Header closeButton>
        <Modal.Title>Add Zone</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="zoneDemand">
            <Form.Label>Demand</Form.Label>
            <Form.Control
              type="number"
              value={zoneData.demand}
              onChange={(e) =>
                setZoneData({ ...zoneData, demand: parseFloat(e.target.value) })
              }
              required
            />
          </Form.Group>
          <Form.Group controlId="zonePriority">
            <Form.Label>Priority</Form.Label>
            <Form.Control
              type="number"
              value={zoneData.priority}
              onChange={(e) =>
                setZoneData({ ...zoneData, priority: parseInt(e.target.value) })
              }
              required
            />
          </Form.Group>
          <Button variant="primary" type="submit" className="mt-3">
            Save Zone
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
}

export default ZoneModal;
