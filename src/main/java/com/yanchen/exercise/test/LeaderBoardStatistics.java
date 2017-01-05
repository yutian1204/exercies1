package com.yanchen.exercise.test;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by yuyanchen on 17/1/5.
 */
public class LeaderBoardStatistics {
    public static final String TO_FILE = "/Users/yuyanchen/LeaderBoardStatistics%d.csv";
    public static final String FROM_FILE = "/Users/yuyanchen/sum%d.txt";
    public static final Charset UTF_8 = Charsets.UTF_8;

    public static final Function<String, LeaderBoard> TRANSFER_FUNCTION = new Function<String, LeaderBoard>() {
        @Override
        public LeaderBoard apply(String line) {
            return new Gson().fromJson(line, LeaderBoard.class);
        }
    };

    public static void main(String[] args) throws IOException {
        for (int i = 0; i <= 6 ; i++) {
            String fromFile = FROM_FILE.replace("%d", String.valueOf(i));
            //读文件
            List<String> lines = Files.readLines(new File(fromFile), UTF_8);
            //解析
            List<LeaderBoard> leaderBoardList = Lists.transform(lines, TRANSFER_FUNCTION);
            //写入文件
            String toFile = TO_FILE.replace("%d", String.valueOf(i));
            writeToFile(toFile, leaderBoardList);
        }
    }

    private static void writeToFile(String toFile, List<LeaderBoard> leaderBoardList) throws IOException {
        File to = new File(toFile);
        Files.write("名次,用户id,昵称,手机号\n", to, UTF_8);

        for (LeaderBoard leaderBoard : leaderBoardList) {
            String line = leaderBoard.getRank() + "," + leaderBoard.getUserId() + "," + leaderBoard.getNickname() + ","
                    + leaderBoard.getMobile() + "\n";
            Files.append(line, to, UTF_8);
        }
    }


    class LeaderBoard {
        private int rank;
        private long userId;
        private BigDecimal profitability;
        private int preRank;
        private String position;
        private int direction;
        private String nickname;
        private String avatar;
        private String mobile;
        private long updateTime;

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public BigDecimal getProfitability() {
            return profitability;
        }

        public void setProfitability(BigDecimal profitability) {
            this.profitability = profitability;
        }

        public int getPreRank() {
            return preRank;
        }

        public void setPreRank(int preRank) {
            this.preRank = preRank;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }
}
