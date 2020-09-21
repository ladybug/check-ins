import React, { useContext, useEffect, useState } from "react";

import {
  UPDATE_CURRENT_CHECKIN,
  UPDATE_CHECKINS,
} from "../../context/AppContext";
import { AppContext } from "../../context/AppContext";
import { updateCheckin } from "../../api/checkins";

import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import CalendarTodayIcon from "@material-ui/icons/CalendarToday";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { Link } from "react-router-dom";

import "./Checkin.css";

const CheckinsHistory = ({ history }) => {
  const { state, dispatch } = useContext(AppContext);
  const { checkins, currentCheckin } = state;
  const [index, setIndex] = useState(0);

  useEffect(() => {
    if (checkins.length) {
      const { pathname } = history.location;
      const [, , checkinid] = pathname.split("/");
      const i = checkinid
        ? checkins.findIndex((checkin) => checkin.id === checkinid)
        : checkins.length - 1;
      setIndex(i);
      const checkin = checkins[index];
      dispatch({ type: UPDATE_CURRENT_CHECKIN, payload: checkin });
      history.push(`/checkins/${checkin.id}`);
    }
  }, [checkins, index, dispatch, history]);

  const getCheckinDate = () => {
    if (currentCheckin && currentCheckin.checkInDate) {
      return new Date(currentCheckin.checkInDate);
    }
    // return new date unless you are running a Jest test
    return process.env.JEST_WORKER_ID ? new Date(2020, 9, 21) : new Date();
  };

  const lastIndex = checkins.length - 1;
  const leftArrowClass = "arrow " + (index > 0 ? "enabled" : "disabled");
  const rightArrowClass =
    "arrow " + (index < lastIndex ? "enabled" : "disabled");

  const previousCheckin = () => {
    if (index !== 0) {
      setIndex((index) => index - 1);
    }
  };

  const nextCheckin = () => {
    if (index !== lastIndex) {
      setIndex((index) => index + 1);
    }
  };

  const pickDate = async (date) => {
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const checkin = checkins[index];
    const dateArray = [year, month, day];
    const updatedCheckin = await updateCheckin({
      ...checkin,
      checkInDate: dateArray,
    });
    const newCheckin = updatedCheckin.payload.data;
    const filtered = checkins.filter((e) => {
      return e.id !== checkin.id;
    });
    filtered.push(newCheckin);
    dispatch({
      type: UPDATE_CHECKINS,
      payload: filtered,
    });
    dispatch({
      type: UPDATE_CURRENT_CHECKIN,
      payload: newCheckin,
    });
  };

  const DateInput = React.forwardRef((props, ref) => (
    <div className="date-input" ref={ref}>
      <p style={{ margin: "0px" }}>{props.value}</p>
      <CalendarTodayIcon onClick={props.onClick}>stuff</CalendarTodayIcon>
    </div>
  ));

  return (
    <div>
      {getCheckinDate() && (
        <div className="date-picker">
          <Link
            onClick={previousCheckin}
            to={`${currentCheckin && currentCheckin.id}`}
          >
            <ArrowBackIcon
              className={leftArrowClass}
              style={{ fontSize: "50px" }}
            />
          </Link>
          <DatePicker
            closeOnScroll
            customInput={<DateInput />}
            dateFormat="MMMM dd, yyyy h:mm aa"
            onChange={pickDate}
            selected={getCheckinDate()}
            showTimeSelect
            withPortal
          />
          <Link
            onClick={nextCheckin}
            to={`${currentCheckin && currentCheckin.id}`}
          >
            <ArrowForwardIcon
              className={rightArrowClass}
              style={{ fontSize: "50px" }}
            />
          </Link>
        </div>
      )}
    </div>
  );
};

export default CheckinsHistory;
