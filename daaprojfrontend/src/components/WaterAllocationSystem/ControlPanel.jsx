import React from "react";

const ControlPanel = ({ onAllocate, isAllocating }) => {
  return (
    <div className="control-panel mb-3">
      <button
        className="btn btn-success"
        onClick={onAllocate}
        disabled={isAllocating}
      >
        {isAllocating ? "Allocating..." : "Allocate Water"}
      </button>
    </div>
  );
};

export default ControlPanel;
