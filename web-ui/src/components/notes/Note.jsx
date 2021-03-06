import React, { useContext, useEffect, useState } from "react";

import {
  getNoteByCheckinId,
  createCheckinNote,
  updateCheckinNote,
} from "../../api/checkins";
import { AppContext } from "../../context/AppContext";

import { debounce } from "lodash/function";
import NotesIcon from "@material-ui/icons/Notes";
import LockIcon from "@material-ui/icons/Lock";
import Skeleton from "@material-ui/lab/Skeleton";

import "./Note.css";

async function realUpdate(note) {
  await updateCheckinNote(note);
}

const updateNote = debounce(realUpdate, 1000);

const Notes = (props) => {
  const { state } = useContext(AppContext);
  const { userProfile, currentCheckin, selectedProfile } = state;
  const { memberProfile } = userProfile;
  const { id } = memberProfile;
  const { memberName } = props;
  const [note, setNote] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  // TODO: get private note
  const [privateNote, setPrivateNote] = useState("Private note");
  const selectedProfilePDLId = selectedProfile && selectedProfile.pdlId;
  const pdlId = memberProfile && memberProfile.pdlId;
  const pdlorAdmin =
    (memberProfile &&
      userProfile.role &&
      userProfile.role.includes("PDL")) ||
      userProfile.role.includes("ADMIN");

  const canViewPrivateNote =
    pdlorAdmin && memberProfile.id !== currentCheckin.teamMemberId;
  const currentCheckinId = currentCheckin && currentCheckin.id;

  useEffect(() => {
    async function getNotes() {
      if (!pdlId) {
        return;
      }
      setIsLoading(true);
      try {
        let res = await getNoteByCheckinId(currentCheckinId);
        if (res.error) throw new Error(res.error);

        const currentNote =
          res.payload && res.payload.data && res.payload.data.length > 0
            ? res.payload.data[0]
            : null;
        if (currentNote) {
          setNote(currentNote);
        } else if (id === selectedProfilePDLId) {
          res = await createCheckinNote({
            checkinid: currentCheckinId,
            createdbyid: id,
            description: "",
          });
          if (res.error) throw new Error(res.error);
          if (res && res.payload && res.payload.data) {
            setNote(res.payload.data);
          }
        }
      } catch (e) {
        console.log(e);
      }
      setIsLoading(false);
    }
    getNotes();
  }, [currentCheckinId, pdlId, id, selectedProfilePDLId]);

  const handleNoteChange = (e) => {
    const { value } = e.target;
    setNote((note) => {
      const newNote = { ...note, description: value };
      updateNote(newNote);
      return newNote;
    });
  };

  const handlePrivateNoteChange = (e) => {
    setPrivateNote(e.target.value);
  };

  return (
    <div className="notes">
      <div>
          <div>
            <h1>
              <NotesIcon style={{ marginRight: "10px" }} />
              Notes for {memberName}
            </h1>
            <div className="container">
              {isLoading ? (
                <div className="skeleton">
                  <Skeleton variant="text" height={"2rem"} />
                  <Skeleton variant="text" height={"2rem"} />
                  <Skeleton variant="text" height={"2rem"} />
                  <Skeleton variant="text" height={"2rem"} />
                </div>
              ) : (
                <textarea
                  disabled={!pdlorAdmin || currentCheckin.completed === true}
                  onChange={handleNoteChange}
                  value={note && note.description ? note.description : ""}
                ></textarea>
              )}
            </div>
          </div>
      </div>
      {canViewPrivateNote && (
        <div>
          <h1>
            <LockIcon style={{ marginRight: "10px" }} />
            Private Notes
          </h1>
          <div className="container">
            <textarea onChange={handlePrivateNoteChange} value={privateNote}>
              <p></p>
            </textarea>
          </div>
        </div>
      )}
    </div>
  );
};

export default Notes;
