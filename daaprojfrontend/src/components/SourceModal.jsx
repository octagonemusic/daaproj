import React, { useState } from "react";
import { Modal, Button, Form } from "react-bootstrap";
import axios from "axios";

function SourceModal({ show, onHide, refreshData }) {
  const [sourceData, setSourceData] = useState({ capacity: 0 });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await axios.post("http://localhost:8080/api/sources", sourceData);
    refreshData();
    onHide();
  };

  return (
    <Modal show={show} onHide={onHide}>
      <Modal.Header closeButton>
        <Modal.Title>Add Source</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group controlId="sourceCapacity">
            <Form.Label>Capacity</Form.Label>
            <Form.Control
              type="number"
              value={sourceData.capacity}
              onChange={(e) =>
                setSourceData({
                  ...sourceData,
                  capacity: parseFloat(e.target.value),
                })
              }
              required
            />
          </Form.Group>
          <Button variant="primary" type="submit" className="mt-3">
            Save Source
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
}

export default SourceModal;
