package com.accesscontrol.hephaestus.ameeting.json;

//获取该会议室的当日将要进行的会议
import java.util.List;

public class GetAllMeeting {

    private int code;
    private String msg;
    private Content data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Content getData() {
        return data;
    }

    public void setData(Content data) {
        this.data = data;
    }

    public class Content{
        private List<Info> reserveInfos;

        public List<Info> getReserveInfos() {
            return reserveInfos;
        }

        public void setReserveInfos(List<Info> reserveInfos) {
            this.reserveInfos = reserveInfos;
        }

        public class Info{
            private int reserveId;
            private String startTime;
            private String endTime;
            int rid;
            int reserveUid;
            String participantStr;
            String participants;
            String topic;
            int flag;
            int reserveOid;
            int reserveDid;

            public int getReserveId() {
                return reserveId;
            }

            public void setReserveId(int reserveId) {
                this.reserveId = reserveId;
            }

            public String getStartTime() {
                return startTime;
            }

            public void setStartTime(String startTime) {
                this.startTime = startTime;
            }

            public String getEndTime() {
                return endTime;
            }

            public void setEndTime(String endTime) {
                this.endTime = endTime;
            }

            public int getRid() {
                return rid;
            }

            public void setRid(int rid) {
                this.rid = rid;
            }

            public int getReserveUid() {
                return reserveUid;
            }

            public void setReserveUid(int reserveUid) {
                this.reserveUid = reserveUid;
            }

            public String getParticipantStr() {
                return participantStr;
            }

            public void setParticipantStr(String participantStr) {
                this.participantStr = participantStr;
            }

            public String getParticipants() {
                return participants;
            }

            public void setParticipants(String participants) {
                this.participants = participants;
            }

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

            public int getFlag() {
                return flag;
            }

            public void setFlag(int flag) {
                this.flag = flag;
            }

            public int getReserveOid() {
                return reserveOid;
            }

            public void setReserveOid(int reserveOid) {
                this.reserveOid = reserveOid;
            }

            public int getReserveDid() {
                return reserveDid;
            }

            public void setReserveDid(int reserveDid) {
                this.reserveDid = reserveDid;
            }
        }
    }

}

//{
//         "reserveId":55,
//        "startTime":"2019-02-15 03:31:00",
//        "endTime":"2019-02-15 04:31:00",
//        "rid":5,
//        "reserveUid":34,
//        "participantStr":null,
//        "participants":null,
//        "topic":"会议A",
//        "flag":0,
//        "reserveOid":5,
//        "reserveDid":1
//}

