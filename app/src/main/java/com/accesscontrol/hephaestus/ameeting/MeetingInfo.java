package com.accesscontrol.hephaestus.ameeting;

public class MeetingInfo {
    int code;
    String msg;
    Data data;
    public  static class Data{
        private static ResrveInfos resrveInfos;

        public class ResrveInfos{
            int reserveId;
            String startTime;
            String endTime;
            int rid;
            String Uid;
            String participants;
            String meetingTopic;

            public int getReserveId() {
                return reserveId;
            }

            public String getStartTime() {
                return startTime;
            }

            public String getEndTime() {
                return endTime;
            }

            public int getRid() {
                return rid;
            }

            public String getUid() {
                return Uid;
            }

            public String getParticipants() {
                return participants;
            }

            public String getMeetingTopic() {
                return meetingTopic;
            }
        }
        public ResrveInfos getResrveInfos(){
            return resrveInfos;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }
}
