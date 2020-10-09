import React from "react";
import cycle from "./checkin-cycle.png";

const CheckinCycle = ({ style }) => (
  <div>
    <img
      style={{ width: "35vw", ...style }}
      alt="The Check-in Cycle including Expectations, Feedback, and Development"
      src={cycle}
    />
  </div>
);

export default CheckinCycle;
