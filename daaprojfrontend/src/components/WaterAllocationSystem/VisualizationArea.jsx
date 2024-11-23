import React, { useState, useEffect } from "react";
import { Card, Row, Col, Badge, Carousel } from "react-bootstrap";
import axios from "axios";

const VisualizationArea = ({
  sources,
  zones,
  connections,
  allocations,
  onElementSelect,
}) => {
  const [connectionDetails, setConnectionDetails] = useState({});
  const extractId = (url) => url.split("/").pop();

  useEffect(() => {
    const fetchConnectionDetails = async () => {
      const details = {};

      for (const connection of connections) {
        try {
          const [sourceRes, zoneRes] = await Promise.all([
            axios.get(connection._links.source.href),
            axios.get(connection._links.zone.href),
          ]);

          details[connection._links.self.href] = {
            sourceId: extractId(sourceRes.data._links.self.href),
            zoneId: extractId(zoneRes.data._links.self.href),
          };
        } catch (error) {
          console.error("Error fetching connection details:", error);
        }
      }

      setConnectionDetails(details);
    };

    if (connections.length > 0) {
      fetchConnectionDetails();
    }
  }, [connections]);

  const calculateSourceFillPercentage = (
    source,
    allocations,
    connections,
    connectionDetails
  ) => {
    const sourceId = extractId(source._links.self.href);

    // Find all connections for this source
    const sourceConnections = connections.filter((conn) => {
      const details = connectionDetails[conn._links.self.href];
      return details?.sourceId === sourceId;
    });

    // Calculate total allocated water from this source
    let totalAllocated = 0;
    sourceConnections.forEach((conn) => {
      const details = connectionDetails[conn._links.self.href];
      if (details) {
        const zoneAllocation = allocations?.find(
          (a) => a.zoneId === parseInt(details.zoneId)
        );
        if (zoneAllocation) {
          totalAllocated += zoneAllocation.amount;
        }
      }
    });

    // Calculate remaining capacity
    const remaining = source.capacity - totalAllocated;
    const percentage = (remaining / source.capacity) * 100;

    return Math.max(0, Math.min(100, percentage)); // Ensure percentage is between 0 and 100
  };

  const calculateZoneFillPercentage = (zone, allocation) => {
    // Use allocation amount if available, otherwise use filledCapacity
    const filled = allocation ? allocation.amount : zone.filledCapacity || 0;
    return (filled / zone.demand) * 100;
  };

  const calculateZoneCost = (
    zone,
    zoneConnections,
    connectionDetails,
    allocations
  ) => {
    const zoneId = extractId(zone._links.self.href);
    const allocation = allocations?.find((a) => a.zoneId === parseInt(zoneId));

    console.log("Cost Calculation Debug:", {
      zoneId,
      allocation,
      connectionAllocations: allocation?.connectionAllocations,
      zoneConnections: zoneConnections.map((conn) => ({
        id: extractId(conn._links.self.href),
        costPerUnit: conn.costPerUnit,
      })),
    });

    if (!allocation || !allocation.connectionAllocations) return 0;

    const totalCost = allocation.connectionAllocations.reduce(
      (total, connAllocation) => {
        console.log("Processing connection allocation:", {
          connectionId: connAllocation.connectionId,
          availableConnections: zoneConnections.map((conn) =>
            extractId(conn._links.self.href)
          ),
        });

        const connection = zoneConnections.find(
          (conn) =>
            parseInt(extractId(conn._links.self.href)) ===
            connAllocation.connectionId
        );

        console.log("Found connection:", connection);

        if (connection) {
          const cost = connAllocation.amount * connection.costPerUnit;
          console.log("Connection cost:", {
            connectionId: connAllocation.connectionId,
            amount: connAllocation.amount,
            costPerUnit: connection.costPerUnit,
            cost,
          });
          return total + cost;
        }
        return total;
      },
      0
    );

    console.log("Total cost:", totalCost);
    return totalCost;
  };

  // Group cards into sets of 3 for the carousel
  const sourceGroups = [];
  const zoneGroups = [];

  for (let i = 0; i < sources.length; i += 3) {
    sourceGroups.push(sources.slice(i, i + 3));
  }

  for (let i = 0; i < zones.length; i += 3) {
    zoneGroups.push(zones.slice(i, i + 3));
  }

  return (
    <div className="visualization-container">
      <div className="sources-section">
        <h4 className="section-title">Sources</h4>
        <Carousel
          interval={null}
          indicators={true}
          controls={sourceGroups.length > 1}
          className="custom-carousel"
        >
          {sourceGroups.map((group, groupIndex) => (
            <Carousel.Item key={groupIndex}>
              <div className="d-flex justify-content-center gap-4">
                {group.map((source) => {
                  const fillPercentage = calculateSourceFillPercentage(
                    source,
                    allocations,
                    connections,
                    connectionDetails
                  );
                  // Calculate remaining capacity for display
                  const sourceId = extractId(source._links.self.href);
                  let totalAllocated = 0;
                  connections
                    .filter(
                      (conn) =>
                        connectionDetails[conn._links.self.href]?.sourceId ===
                        sourceId
                    )
                    .forEach((conn) => {
                      const details = connectionDetails[conn._links.self.href];
                      const zoneAllocation = allocations?.find(
                        (a) => a.zoneId === parseInt(details?.zoneId)
                      );
                      if (zoneAllocation) {
                        totalAllocated += zoneAllocation.amount;
                      }
                    });

                  const remainingCapacity = source.capacity - totalAllocated;

                  return (
                    <Card
                      key={source._links.self.href}
                      className="source-card"
                      onClick={() => onElementSelect(source)}
                    >
                      <Card.Body>
                        <Card.Title>Source {sourceId}</Card.Title>
                        <div className="water-level-indicator">
                          <div
                            className="water-fill"
                            style={{
                              height: `${fillPercentage}%`,
                            }}
                          />
                          <div className="water-percentage">
                            {Math.round(fillPercentage)}%
                          </div>
                        </div>
                        <div className="mt-2">
                          <div>Capacity: {source.capacity}</div>
                          <div>Remaining: {Math.max(0, remainingCapacity)}</div>
                        </div>
                      </Card.Body>
                    </Card>
                  );
                })}
              </div>
            </Carousel.Item>
          ))}
        </Carousel>
      </div>

      <div className="zones-section mt-5">
        <h4 className="section-title">Zones</h4>
        <Carousel
          interval={null}
          indicators={true}
          controls={zoneGroups.length > 1}
          className="custom-carousel"
        >
          {zoneGroups.map((group, groupIndex) => (
            <Carousel.Item key={groupIndex}>
              <div className="d-flex justify-content-center gap-4">
                {group.map((zone) => {
                  const zoneId = extractId(zone._links.self.href);
                  const allocation = allocations?.find(
                    (a) => a.zoneId === parseInt(zoneId)
                  );
                  const fillPercentage = calculateZoneFillPercentage(
                    zone,
                    allocation
                  );
                  const zoneConnections = connections.filter((conn) => {
                    const details = connectionDetails[conn._links.self.href];
                    return details?.zoneId === zoneId;
                  });

                  const totalCost = calculateZoneCost(
                    zone,
                    zoneConnections,
                    connectionDetails,
                    allocations
                  );

                  console.log("Zone:", {
                    id: zoneId,
                    demand: zone.demand,
                    filled: zone.filledCapacity,
                    fillPercentage,
                    totalCost,
                  });

                  return (
                    <Card
                      key={zone._links.self.href}
                      className="zone-card"
                      onClick={() => onElementSelect(zone)}
                    >
                      <Card.Body>
                        <Card.Title>
                          Zone {zoneId}{" "}
                          <Badge bg="info">Priority {zone.priority}</Badge>
                        </Card.Title>
                        <div className="water-level-indicator">
                          <div
                            className="water-fill"
                            style={{
                              height: `${fillPercentage}%`,
                            }}
                          />
                          <div className="water-percentage">
                            {Math.round(fillPercentage)}%
                          </div>
                        </div>
                        <div className="mt-2">
                          <div>Demand: {zone.demand}</div>
                          <div>
                            Filled: {allocation ? allocation.amount : 0}
                          </div>
                          <div>Total Cost: ${totalCost.toFixed(2)}</div>
                        </div>
                        <div className="mt-2">
                          <small>Connected Sources:</small>
                          <div className="connection-list">
                            {zoneConnections.map((conn) => {
                              const details =
                                connectionDetails[conn._links.self.href];
                              const allocation = allocations?.find(
                                (a) => a.zoneId === parseInt(zoneId)
                              );
                              const connAllocation =
                                allocation?.connectionAllocations?.find(
                                  (ca) =>
                                    ca.connectionId ===
                                    parseInt(extractId(conn._links.self.href))
                                );

                              return details ? (
                                <div
                                  key={conn._links.self.href}
                                  className="connection-item d-flex justify-content-between align-items-center mb-1"
                                >
                                  <div>
                                    <Badge bg="secondary" className="me-2">
                                      S{details.sourceId}
                                    </Badge>
                                    <small className="text-muted">
                                      ${conn.costPerUnit}/unit
                                    </small>
                                  </div>
                                  {connAllocation && (
                                    <small className="text-muted">
                                      {connAllocation.amount.toFixed(1)} units
                                    </small>
                                  )}
                                </div>
                              ) : null;
                            })}
                          </div>
                        </div>
                      </Card.Body>
                    </Card>
                  );
                })}
              </div>
            </Carousel.Item>
          ))}
        </Carousel>
      </div>
    </div>
  );
};

export default VisualizationArea;
