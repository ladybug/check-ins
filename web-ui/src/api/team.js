import axios from "axios";
import { resolve, BASE_API_URL } from "./api.js";

const teamUrl = `${BASE_API_URL}/services/team`;
const teamMemberUrl = `${BASE_API_URL}/services/team/member`;
export const getAllTeamMembers = async () => {
  return await resolve(
    axios({
      method: "get",
      url: teamMemberUrl,
      responseType: "json",
      withCredentials: true
    })
  );
};

export const getMembersByTeam = async (id) => {
  return await resolve(
    axios({
      method: "get",
      url: teamMemberUrl,
      responseType: "json",
      params: {
        teamid: id,
      },
      withCredentials: true
    })
  );
};

export const getTeamsByMember = async (id) => {
  return await resolve(
    axios({
      method: "get",
      url: teamMemberUrl,
      responseType: "json",
      params: {
        memberid: id,
      },
      withCredentials: true
    })
  );
};

export const getAllTeams = async () => {
  return await resolve(
    axios({
      method: "get",
      url: teamUrl,
      responseType: "json",
      withCredentials: true
    })
  )
};

export const getTeam = async (id) => {
  return await resolve(
    axios({
      method: "get",
      url: `${teamUrl}/${id}`,
      responseType: "json",
      withCredentials: true
    })
  )
};

export const createTeam = async(team) => {
    return await resolve(
        axios({
            method: "post",
            url: teamUrl,
            responseType: "json",
            data: team,
            withCredentials: true,
        })
    );
};

export const updateTeam = async(team) => {
    return await resolve(
        axios({
            method: "put",
            url: teamUrl,
            responseType: "json",
            data: team,
            withCredentials: true,
        })
    );
};

export const addTeamMember = async(teamMember) => {
    return await resolve(
        axios({
            method: "post",
            url: teamMemberUrl,
            responseType: "json",
            data: teamMember,
            withCredentials: true,
        })
    );
};

export const updateTeamMember = async(teamMember) => {
    return await resolve(
        axios({
            method: "put",
            url: teamMemberUrl,
            responseType: "json",
            data: teamMember,
            withCredentials: true,
        })
    );
};
