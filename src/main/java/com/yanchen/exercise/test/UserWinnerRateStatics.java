package com.yanchen.exercise.test;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 统计比赛一周用户的完胜率
 * sql:
 * select t.userId,t.symbol,  t.price,t.type,t.quantity, t.time from t_transaction t
 * join t_user u on u.userId = t.userId
 * where
 * u.activityId = 'winterGame' and t.time>= 1479087000000 and t.time < 1479691800000
 * order by t.userId,t.symbol
 * <p>
 * Created by yuyanchen on 16/11/22.
 */
@Service
public class UserWinnerRateStatics {
    public static final Splitter SPLITTER = Splitter.on("\t").omitEmptyStrings().trimResults();
    public static final int TYPE_BUY = 1;
    public static final int TYPE_SELL = -1;
    public static final String TO_FILE = "/Users/yuyanchen/userResult.csv";
    public static final String FROM_FILE = "/Users/yuyanchen/userTrans.csv";
    public static final Charset UTF_8 = Charsets.UTF_8;

    //数据库0:买 1:卖 ,映射转换
    public static final Map<Integer, Integer> TYPE_MAPPING = ImmutableMap.of(0, TYPE_BUY, 1, TYPE_SELL);

    public static final Function<String, Transaction> TRANSFER_FUNCTION = new Function<String, Transaction>() {
        @Override
        public Transaction apply(String line) {
            List<String> fields = Lists.newArrayList(SPLITTER.split(line));
            Transaction transaction = new Transaction();
            transaction.setUserId(Integer.valueOf(fields.get(0).trim()));
            transaction.setSymbol(fields.get(1).trim());
            transaction.setPrice(new BigDecimal(fields.get(2).trim()));
            Integer type = Integer.valueOf(fields.get(3).trim());
            transaction.setType(TYPE_MAPPING.get(type));
            int quantity = new BigDecimal(fields.get(4).trim()).intValue();
            transaction.setQuantity(quantity);
            transaction.setTime(fields.get(5).trim());
            return transaction;
        }
    };

    public static void main(String[] args) throws IOException {
        //读文件
        List<String> lines = Files.readLines(new File(FROM_FILE), UTF_8);
        lines.remove(0);
        //解析
        List<Transaction> transactionList = Lists.transform(lines, TRANSFER_FUNCTION);
        //根据用户,标的统计
        Map<String, List<Transaction>> userSymbols = mapToUserSymbols(transactionList);
        //统计用户的完胜率
        List<UserStatics> userList = staticsUserWinnerRate(userSymbols);
        //写入文件
        writeToFile(userList);
    }

    private static List<UserStatics> staticsUserWinnerRate(Map<String, List<Transaction>> userSymbols) {
        int sum = 0;
        int succSum = 0;
        //Map<userId, profit>
        Map<Integer, UserStatics> userProfit = Maps.newHashMap();

        for (Map.Entry<String, List<Transaction>> entry : userSymbols.entrySet()) {
            UserStatics userStatics = statisticTransaction(entry.getValue());
            sum += userStatics.getSumCount();
            succSum += userStatics.getSuccCount();
            int userId = userStatics.getUserId();
            UserStatics statics = userProfit.get(userId);
            if (null == statics) {
                statics = userStatics;
            } else {
                statics = statics.add(userStatics);
            }
            userProfit.put(userId, statics);
        }

        System.out.println("sum:" + sum + ",succSum" + succSum);
        List<UserStatics> userStaticses = Lists.newArrayList(userProfit.values());
        userStaticses.sort(new Comparator<UserStatics>() {
            @Override
            public int compare(UserStatics o1, UserStatics o2) {
                return o2.getProfitRate().compareTo(o1.getProfitRate());
            }
        });

        return userStaticses;
    }

    private static void writeToFile(List<UserStatics> userStaticsList) throws IOException {
        File to = new File(TO_FILE);
        Files.write("用户id,完胜率,总共收益,收益次数,交易次数\n", to, UTF_8);

        for (UserStatics userStatics : userStaticsList) {
            String line = userStatics.getUserId() + "," + userStatics.getProfitRate() + "," + userStatics.getSumProfit()
                    + "," + userStatics.getSuccCount() + "," + userStatics.getSumCount() + "\n";
            Files.append(line, to, UTF_8);
        }
    }

    private static Map<String, List<Transaction>> mapToUserSymbols(List<Transaction> transactionList) {
        Map<String, List<Transaction>> userSymbols = Maps.newHashMap();
        for (Transaction transaction : transactionList) {
            String key = transaction.getUserId() + ":" + transaction.getSymbol();
            List<Transaction> list = userSymbols.get(key);
            if (CollectionUtils.isEmpty(list)) {
                userSymbols.put(key, Lists.newArrayList(transaction));
            } else {
                list.add(transaction);
                userSymbols.put(key, list);
            }
        }
        return userSymbols;
    }

    private static UserStatics statisticTransaction(List<Transaction> list) {
        //eg init: buy
        int initType = list.get(0).getType();
        int startDescIdx = 0;

        for (int i = 1; i < list.size(); i++) {
            Transaction src = list.get(i);
            if (initType == src.getType()) {
                continue;
            }

            //src:sell
            for (int idx = startDescIdx; idx < i; ++idx) {
                if (src.getQuantity() == 0) {
                    continue;
                }

                Transaction desc = list.get(idx);

                //decs:buy
                if (src.getQuantity() <= 0 || 0 == desc.getQuantity() || initType != desc.getType()) {
                    continue;
                }
                int srcQuantity = src.getQuantity();
                int descQuantity = desc.getQuantity();
                int minQuantity;
                if (srcQuantity >= descQuantity) {
                    ++startDescIdx;
                    src.setIntegrity(true);
                    desc.setQuantity(0);
                    src.setQuantity(srcQuantity - descQuantity);
                    minQuantity = descQuantity;
                } else {
                    src.setQuantity(0);
                    desc.setQuantity(srcQuantity - descQuantity);
                    minQuantity = srcQuantity;
                }

                BigDecimal srcPrice = src.getPrice().multiply(new BigDecimal(minQuantity)).multiply(new BigDecimal(src.getType()));
                BigDecimal descPrice = desc.getPrice().multiply(new BigDecimal(minQuantity)).multiply(new BigDecimal(desc.getType()));
                BigDecimal profit = srcPrice.add(descPrice);
                src.setProfit(profit);
            }
        }

        Integer succSum = 0;
        Integer sum = 0;
        BigDecimal sumProfit = BigDecimal.ZERO;
        for (Transaction transaction : list) {
            sumProfit = sumProfit.add(transaction.getProfit());
            if (transaction.isIntegrity()) {
                ++sum;
                if (transaction.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                    ++succSum;
                }
            }
        }

        UserStatics userStatics = new UserStatics();
        userStatics.setSuccCount(succSum);
        userStatics.setSumCount(sum);
        userStatics.setSumProfit(sumProfit);
        userStatics.setUserId(list.get(0).userId);
        userStatics.setSymbol(list.get(0).symbol);
        return userStatics;
    }

    private static class UserStatics {
        int userId;
        String symbol;
        int succCount = 0;
        int sumCount = 0;
        BigDecimal sumProfit = BigDecimal.ZERO;
        BigDecimal profitRate = BigDecimal.ZERO;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public int getSuccCount() {
            return succCount;
        }

        public void setSuccCount(int succCount) {
            this.succCount = succCount;
        }

        public int getSumCount() {
            return sumCount;
        }

        public void setSumCount(int sumCount) {
            this.sumCount = sumCount;
        }

        public BigDecimal getSumProfit() {
            return sumProfit;
        }

        public void setSumProfit(BigDecimal sumProfit) {
            this.sumProfit = sumProfit;
        }

        public BigDecimal getProfitRate() {
            if (0 == succCount || 0 == sumCount) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(1.0 * succCount / sumCount);
        }

        public void setProfitRate(BigDecimal profitRate) {
            this.profitRate = profitRate;
        }

        public UserStatics add(UserStatics userStatics) {
            this.succCount += userStatics.getSuccCount();
            this.sumProfit = this.sumProfit.add(userStatics.getSumProfit());
            this.sumCount += userStatics.getSumCount();
            return this;
        }
    }

    private static class Transaction {
        int userId;
        String symbol;
        BigDecimal price;
        int type;
        int quantity;
        String time;
        BigDecimal profit = BigDecimal.ZERO;
        boolean integrity = false;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getProfit() {
            return profit;
        }

        public void setProfit(BigDecimal profit) {
            this.profit = profit;
        }

        public boolean isIntegrity() {
            return integrity;
        }

        public void setIntegrity(boolean integrity) {
            this.integrity = integrity;
        }
    }
}
