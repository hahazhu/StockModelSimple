CREATE TABLE `stock_index` (
  `STOCK_ID` varchar(6) DEFAULT NULL,
  `MARKET` varchar(2) DEFAULT NULL,
  `VOLUME` decimal(18,0) DEFAULT NULL,
  `OPEN` decimal(10,4) DEFAULT NULL,
  `HIGH` decimal(10,4) DEFAULT NULL,
  `CLOSE` decimal(10,4) DEFAULT NULL,
  `LOW` decimal(10,4) DEFAULT NULL,
  `CHG` decimal(10,4) DEFAULT NULL,
  `PERCENT` decimal(10,4) DEFAULT NULL,
  `TURNRATE` decimal(10,4) DEFAULT NULL,
  `MA5` decimal(10,4) DEFAULT NULL,
  `MA10` decimal(10,4) DEFAULT NULL,
  `MA20` decimal(10,4) DEFAULT NULL,
  `MA30` decimal(10,4) DEFAULT NULL,
  `EMA12` decimal(10,4) DEFAULT NULL,
  `EMA26` decimal(10,4) DEFAULT NULL,
  `DIF` decimal(10,4) DEFAULT NULL,
  `DEA` decimal(10,4) DEFAULT NULL,
  `MACD` decimal(10,4) DEFAULT NULL,
  `D_DATE` varchar(8) DEFAULT NULL,
  `TOMO_PERCENT` decimal(10,4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
