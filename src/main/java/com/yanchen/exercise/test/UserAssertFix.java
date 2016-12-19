package com.yanchen.exercise.test;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yuyanchen on 16/12/18.
 */
@Service
public class UserAssertFix {

    public static final Splitter SPLITTER = Splitter.on("\t").omitEmptyStrings().trimResults();
    public static final int TYPE_BUY = 1;
    public static final int TYPE_SELL = -1;
    public static final String TO_FILE = "/Users/yuyanchen/1000039250.csv";
    public static final String FROM_FILE1 = "/Users/yuyanchen/1000039250.assert.log.81";
    public static final String FROM_FILE2 = "/Users/yuyanchen/1000039250.assert.log.82";
    public static final Charset UTF_8 = Charsets.UTF_8;
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final List<DateTime> date_times = Lists.newArrayList(
            new DateTime(2016, 11, 21, 9, 30, 0),
            new DateTime(2016, 11, 28, 9, 30, 0),
            new DateTime(2016, 12, 5, 9, 30, 0),
            new DateTime(2016, 12, 12, 9, 30, 0),
            new DateTime(2016, 12, 19, 9, 30 ,0));

    public static final Long userId = 1000039250L;

    public static final Function<String, ReceiveTransactionUpload> TRANSFER_FUNCTION = new Function<String, ReceiveTransactionUpload>() {
        @Override
        public ReceiveTransactionUpload apply(String line) {

            int index = StringUtils.indexOf(line, "\"");
            String json = line.substring(index - 1);
            ReceiveTransactionUpload receiveTransactionUpload = new ReceiveTransactionUpload();
            receiveTransactionUpload = new Gson().fromJson(json, ReceiveTransactionUpload.class);
            return receiveTransactionUpload;
        }
    };

    public static void main(String[] args) throws Exception {
        //读文件
        List<String> lines = Files.readLines(new File(FROM_FILE1), UTF_8);
        lines.addAll(Files.readLines(new File(FROM_FILE2), UTF_8));
        //解析
        List<ReceiveTransactionUpload> list = Lists.transform(lines, TRANSFER_FUNCTION);
        //根据用户,标的统计
        Map<Long, Asset> userSymbols = handleList(list);
        //写入文件
        writeToFile(userSymbols);
    }

    private static Map<Long, Asset> handleList(List<ReceiveTransactionUpload> list) {
        Map<Long, Asset> map = Maps.newTreeMap();
        for (ReceiveTransactionUpload receiveTransactionUpload : list) {
            List<ReceiveTransactionUploadItem> items = receiveTransactionUpload.getItems();
            Collection<ReceiveTransactionUploadItem> filter = Collections2.filter(items, new Predicate<ReceiveTransactionUploadItem>() {
                @Override
                public boolean apply(ReceiveTransactionUploadItem input) {
                    return input.getUserId() == userId;
                }
            });
            if (!CollectionUtils.isEmpty(filter)) {
                Asset asset = Lists.newArrayList(filter).get(0).getAsset();
                map.put(receiveTransactionUpload.getServerTime(), asset);
            }
        }

        Map<Long, Asset> result = Maps.newTreeMap();
        for (DateTime dateTime : date_times) {
            long dateTimeMillis = dateTime.getMillis();
            long lastTime = dateTimeMillis;

            for (long serviceTime : map.keySet()) {
                if (serviceTime <= dateTimeMillis) {
                    lastTime = serviceTime;
                } else {
                    Asset asset = map.get(lastTime);
                    result.put(lastTime, asset);
                    break;
                }
            }
        }
        return result;
    }

    private static void writeToFile(Map<Long, Asset> userSymbols) throws Exception {
        File to = new File(TO_FILE);
        Files.write("", to, UTF_8);
        for (Map.Entry<Long, Asset> entry : userSymbols.entrySet()) {
            Asset asset = entry.getValue();
            BigDecimal current = asset.getNetLiq().multiply(exchangeRateToDollar(asset.getCurrency()));
            String line = entry.getKey() + "," + current.toPlainString() + "\r\n";
            Files.append(line, to, UTF_8);
        }
    }

    public static BigDecimal exchangeRateToDollar(String currency) throws Exception {
        if ("USD".equalsIgnoreCase(currency)) {
            return BigDecimal.ONE;
        } else {
            throw new Exception("currency not USD");
        }
    }


    public static class ReceiveTransactionUpload {
        private long serverTime;
        private List<ReceiveTransactionUploadItem> items;

        public long getServerTime() {
            return serverTime;
        }

        public void setServerTime(long serverTime) {
            this.serverTime = serverTime;
        }

        public List<ReceiveTransactionUploadItem> getItems() {
            return items;
        }

        public void setItems(List<ReceiveTransactionUploadItem> items) {
            this.items = items;
        }
    }

    public static class ReceiveTransactionUploadItem {
        private long userId;
        private Asset asset;
        private List<Transaction> transactions;

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public Asset getAsset() {
            return asset;
        }

        public void setAsset(Asset asset) {
            this.asset = asset;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }

    }

    public static class Asset {
        private BigDecimal netLiq;
        private String position;
        private Boolean isLong;
        private String currency;

        public BigDecimal getNetLiq() {
            return netLiq;
        }

        public void setNetLiq(BigDecimal netLiq) {
            this.netLiq = netLiq;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public Boolean getLong() {
            return isLong;
        }

        public void setLong(Boolean aLong) {
            isLong = aLong;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    public static class Transaction {
        private String uuid;
        private String symbol;
        private int quantity;
        private BigDecimal price;
        private long time;
        private short type;
        private String currency;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public short getType() {
            return type;
        }

        public void setType(short type) {
            this.type = type;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

    }
}
