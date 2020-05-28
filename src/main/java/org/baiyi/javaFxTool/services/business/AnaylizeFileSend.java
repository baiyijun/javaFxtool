package org.baiyi.javaFxTool.services.business;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.system.SystemUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.baiyi.javaFxTool.model.business.Co_ToExData;
import org.baiyi.javaFxTool.model.business.Co_Trancodeconfig_Tmp;
import org.baiyi.javaFxTool.model.business.Co_TxtAnalyze;
import org.baiyi.javaFxTool.model.business.ParaTransportVO;
import org.baiyi.javaFxTool.services.tool.FileAnaylizeService;
import org.baiyi.javaFxTool.utils.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: bai
 * @Date: 2019/8/24 14:38
 * @Description: 文件解析神器
 */
@Component
public class AnaylizeFileSend {

    private static final Logger logger = LoggerFactory.getLogger(AnaylizeFileSend.class);

    //本地解析路径
    private ApplicationContext applicationContext = null;

    private static Map<String, Object> methodMap = new HashMap<String, Object>();

    private static Map<String, Object> txtMap = new HashMap<String, Object>();
    //用户路径
    private String userHomePath;

    private FileAnaylizeService fileAnaylizeService;

    //初始化赋值方法
    private static void initMethodMap() {
        Method[] co_ToExData = Co_ToExData.class.getMethods();
        for (Method method : co_ToExData) {
            methodMap.put(method.getName().toUpperCase(), method);
        }
        Method[] co_txtanalyze = Co_TxtAnalyze.class.getMethods();
        for (Method method : co_ToExData) {
            txtMap.put(method.getName().toUpperCase(), method);
        }
    }

    /**
     * 利用java7中的api监控 文件变化
     *
     * @Author: bai
     * @Date: 2019/8/24 15:47
     */
    public void makeWatch(String filepath, FileAnaylizeService fileAnaylizeService) {
        try {
            initMethodMap();
            applicationContext = ApplicationContextHolder.getApplicationContext();
            this.fileAnaylizeService = fileAnaylizeService;
            userHomePath = FileUtil.getUserHomePath() + File.separator + "desktop" + File.separator + "解析结果";
            //解析buscd
            String mark = getDataSetCodeFormFile(filepath);
            logger.info("本次解析的数据包buscd-->" + mark);
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本次解析的数据包buscd-->" + mark + "\n");
            //文件解析
            receiveFile(filepath, mark);
        } catch (Exception e) {
            logger.error(e.toString());
            //e.printStackTrace();
        }
    }

    //获取数据包编号
    private String getDataSetCodeFormFile(String path) throws Exception {
        String dataSetCode = "";
        logger.info("path--->>" + path);

        if (!FileUtil.exist(path)) {
            throw new Exception("文件存储路径下文件：" + path + ",无法访问！");
        }
        if (!FileUtil.isFile(path)) {
            throw new Exception("文件存储路径下文件：" + path + ",非文件！");
        }
        // 文件名称
        String fileName = FileUtil.getName(path);
        // 文件内容
        byte[] content = FileUtil.readBytes(path);

        // 本地化记录
        HashMap<String, Object> hashMap = new HashMap<String, Object>();// 本地化参数容器
        hashMap.put("FileName", fileName);
        hashMap.put("FileByte", content);
        hashMap.put("MyCode", fileName.split("_")[1]);
        hashMap.put("OrgCode", fileName.split("_")[0]);

        this.localize(hashMap);

        ParaTransportVO _ParaTransportVO = new ParaTransportVO();
        _ParaTransportVO.setFilePath((String) hashMap.get("FilePath"));
        _ParaTransportVO.setFileName(fileName);
        // 解压
        hashMap.clear();
        hashMap.put("paraVO", _ParaTransportVO);
        unCompressZip(hashMap);
        // 读取数据文件中的数据集标识
        String filePath = userHomePath + File.separator + "send" + File.separator + DateUtil.today() + File.separator + fileName.split("\\.")[0];
        File f = new File(filePath);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileN = file.getName();
            if (!fileN.toUpperCase().equals("INDEX.TXT")) {
                // 截取数据文件中的数据集标识
                dataSetCode = fileN.split("\\.")[0].split("_")[2];
                break;
            }
        }
        return dataSetCode;
    }

    //文件本地化存储
    private void localize(HashMap<String, Object> paraMap) throws Exception {
        String fileName = (String) paraMap.get("FileName");
        String filePath = "";
        if (null == fileName || "".equals(fileName)) {
            logger.error("filename 为空");
            filePath = userHomePath + File.separator + "send" + File.separator + DateUtil.today();
            fileName = DateUtil.now() + ".xml";
            paraMap.remove("FilePath");
            paraMap.put("FilePath", filePath);
            paraMap.remove("FileName");
            paraMap.put("FileName", fileName);
        } else {
            logger.error("filepath 为空");
            filePath = userHomePath + File.separator + "send" + File.separator + DateUtil.today();
            paraMap.remove("FilePath");
            paraMap.put("FilePath", filePath);
        }
        if (saveFile(paraMap)) {
            logger.info("本地化物理文件成功");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本地化物理文件成功" + "\n");
        } else {
            logger.error("本地化物理文件失败");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本地化物理文件失败" + "\n");
        }
    }

    //文件存储
    private boolean saveFile(HashMap<String, Object> paraMap) throws Exception {
        boolean saveFlag = false;
        String filePath = (String) paraMap.get("FilePath");
        String fileName = (String) paraMap.get("FileName");
        byte[] fileByte = (byte[]) paraMap.get("FileByte");

        File outFilePath = new File(filePath);
        File outFile = new File(filePath + File.separator + fileName);
        if (!outFilePath.exists()) {
            //判断目录是否存在
            logger.info("该文件不存在，在此创建！");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "该文件不存在，在此创建！" + "\n");
            outFilePath.mkdirs();
        }
        if (outFile.exists()) {
            //已下载过的文件无须再本地话
            saveFlag = true;
        } else {
            FileOutputStream fos = null;
            try {
                // 存储
                fos = new FileOutputStream(outFile);
                fos.write(fileByte);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw e;
            } finally {
                fos.close();
            }
            // 再次判断
            if (outFile.exists()) {
                saveFlag = true;
            }
        }
        return saveFlag;
    }

    //解压文件
    private void unCompressZip(HashMap<String, Object> mHashMap) {
        ParaTransportVO pvo = new ParaTransportVO();
        pvo = (ParaTransportVO) mHashMap.get("paraVO");
        // 开始解压zip文件
        logger.info("开始解压文件");
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "开始解压文件" + "\n");
        // 解压之前的文件路径
        String zipFilePath = pvo.getFilePath() + File.separator + pvo.getFileName();
        // 解压之后的路径
        String zipToPath = pvo.getFilePath() + File.separator + pvo.getFileName().split("\\.")[0];// 解压之后的文件路径
        logger.info("文件解压前后路径为：" + zipFilePath + "，" + zipToPath);
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "文件解压前后路径为：" + zipFilePath + "，" + zipToPath + "\n");
        unZip(zipFilePath, zipToPath);
        logger.info("解压zip完毕");
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "解压zip完毕" + "\n");
    }

    //存放解压后文件
    private void unZip(String zipFilePath, String descDir) {
        try {
            ZipUtil.unzip(zipFilePath, descDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void receiveFile(String path, String flag) throws Exception {
        logger.info("传输过来的标识：" + flag);
        FileUtil fileUtil = null;
        if (!FileUtil.exist(path)) {
            throw new Exception("文件存储路径下文件：" + path + ",无法访问！");
        }
        if (!FileUtil.isFile(path)) {
            throw new Exception("文件存储路径下文件：" + path + ",非文件！");
        }
        // 文件名称
        String zipfileName = FileUtil.getName(path);
        // 文件内容
        byte[] content = FileUtil.readBytes(path);

        //本地存储文件的路径
        String localFilePath = userHomePath + File.separator + "send" + File.separator + DateUtil.today();
        //本地化记录
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("FilePath", localFilePath);
        hashMap.put("FileName", zipfileName);
        hashMap.put("FileByte", content);
        hashMap.put("MyCode", zipfileName.split("_")[1]);
        hashMap.put("OrgCode", zipfileName.split("_")[0]);
        localize(hashMap);

        //获取数据流号
        ParaTransportVO _ParaTransportVO = new ParaTransportVO();
        String sendCode = zipfileName.split("_")[0];
        _ParaTransportVO.setFilePath(localFilePath);
        _ParaTransportVO.setFileName(zipfileName);
        _ParaTransportVO.setReceiveCode(zipfileName.split("_")[1]);//我方是接收方
        _ParaTransportVO.setSendCode(sendCode);//发送方是对方

        HashMap<String, Object> hmPara = new HashMap<String, Object>();
        hmPara.put("paraVO", _ParaTransportVO);
        if (_ParaTransportVO.getReceiveCode().indexOf("C") > -1) {
            analyticFeedback(hmPara, flag);
        } else if (_ParaTransportVO.getReceiveCode().indexOf("I") > -1) {//投管解析
            analyticFeedbackIvst(hmPara, flag);
        } else {//受托人与受托人
            analyticFeedbackTrust(hmPara, flag);
        }
    }

    //解析托管
    private void analyticFeedback(HashMap<String, Object> map, String dataSetCode) throws SQLException {
        try {
            //解析文件
            logger.info("本次数据包为发送托管");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本次数据包为发送托管" + "\n");
            parseFile(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解析受托
    private void analyticFeedbackTrust(HashMap<String, Object> map, String dataSetCode) {
        try {
            //解析文件
            logger.info("本次数据包为发送受托");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本次数据包为发送托管" + "\n");
            parseFile(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解析投管
    private void analyticFeedbackIvst(HashMap<String, Object> map, String dataSetCode) {
        try {
            //解析文件
            logger.info("本次数据包为发送投管");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "本次数据包为发送托管" + "\n");
            parseFile(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解析文件
    private String parseFile(HashMap<String, Object> hm) {
        String dataSetCode = null;
        try {
            ParaTransportVO pvo = new ParaTransportVO();
            pvo = (ParaTransportVO) hm.get("paraVO");
            // 文件路路径
            String filePath = pvo.getFilePath() + File.separator + pvo.getFileName().split("\\.")[0];
            File f = new File(filePath);
            File[] files = f.listFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileN = file.getName();

                if (fileN.toUpperCase().equals("INDEX.TXT")) {
                    // 解析索引文件
                    analyIndexFile(file, pvo, files);
                } else if (fileN.split("\\.")[1].trim().toUpperCase().equals("TXT")) {
                    dataSetCode = decrypterDataFile(file, pvo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSetCode;
    }

    //解析索引文件
    private void analyIndexFile(File f, ParaTransportVO pvo, File[] files) throws Exception {
        logger.info("解析索引文件开始！");
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "解析索引文件开始！" + "\n");
        BufferedReader br;
        String lineValue = null;
        try {
            String charset = "GBK";
            if (pvo.getReceiveCode().indexOf("T") > -1) {
                charset = "UTF-8";
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));

            int flagNum = 0;// 计数
            int dataNum = 0;// 数值的行数
            String endLine = "";// 文件中最后一行的数据

            while ((lineValue = br.readLine()) != null) {
                flagNum++;
                // 判读读取到的行是否为空行
                if (lineValue.length() > 0) {

                    if (flagNum == 1) {
                        if (!pvo.getFileName().split("\\.")[0].split("_")[3].equals(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "的值与指令包的编号不同！";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 2) {
                        if (!"TXT".equalsIgnoreCase(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 3) {
                        if (!"V1.0".equalsIgnoreCase(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 4) {
                        if (lineValue.trim() == null && "".equals(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 5) {
                        if (!pvo.getReceiveCode().equals(lineValue)) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 6) {
                        if (!pvo.getFileName().split("\\.")[0].split("_")[2].equals(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行日期与压缩包日期不一致！";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 7) {
                        if (lineValue.trim() == null && "".equals(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 8) {
                        //接受方名称
                        if (lineValue.trim() == null && "".equals(lineValue.trim())) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 9) {
                        try {
                            dataNum = Integer.parseInt(lineValue);
                        } catch (NumberFormatException e) {
                            String errorInfo = "index_文件第" + flagNum + "行数据值不正确，请检查导入文件!";
                            logger.error("errInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");

                        }
                    }
                    try {
                        //由于前headNum行为表头部分， 则headNum+1行为数据项数量的行
                        if (flagNum > 9 && flagNum <= 9 + dataNum) {
                            int num = 0;
                            for (int i = 0; i < files.length; i++) {
                                File file = files[i];
                                String fileN = file.getName();
                                if (!fileN.toUpperCase().equals("INDEX.TXT")) {
                                    if (!fileN.equals(lineValue)) {
                                        num++;
                                    }
                                }
                            }
                            // 判断index中文件与数据内数据文件名称是否一致
                            if (num == files.length - 1) {
                                String errorInfo = "index_文件名" + lineValue + "与实际文件名不一致！";
                                logger.error("errInfo-->" + errorInfo);
                                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");

                            }
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        String errorInfo = "index_导入文件的记录数与recordNumber对应的数字不一致，请检查导入文件!";
                        logger.error("errInfo-->" + errorInfo);
                        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");

                    }
                    // 判断是否有空行
                } else if (lineValue.length() < 1) {
                    String errorInfo = "index_文件第" + flagNum + "行数据为空，请检查导入文件!";
                    logger.error("errInfo-->" + errorInfo);
                    fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                }
                endLine = lineValue;
            }
            if ("".equals(endLine) || endLine == null) {
                String errorInfo = "index_文件中最后一行未找到END结束符，请检查导入文件!";
                logger.error("errInfo-->" + errorInfo);
                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");

            }
            br.close();
            logger.info("解析索引文件结束！");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "解析索引文件结束！" + "\n");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private String decrypterDataFile(File f, ParaTransportVO pvo) {
        logger.info("解析数据文件开始！");
        BufferedReader br = null;
        String lineValue = null;
        String dataSetCode = null;
        try {
            String fileName = f.getName();
            String errorFileName = fileName.split("\\.")[0];// 错误文件名称
            dataSetCode = fileName.split("_")[2];
            String charset = "GBK";
            //文件头行数
            int headNum = 9;
            //查询标志
            String bmly = "";
            if (fileName.split("_")[1].startsWith("T")) {
                bmly = "04";
            } else if (fileName.split("_")[1].startsWith("C")) {
                bmly = "02";
            } else if (fileName.split("_")[1].startsWith("I")) {
                bmly = "03";
            }
            if ("04".equals(bmly)) {
                charset = "UTF-8";
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            // 获取到TXT配置表中的字段名
            logger.info("开始获取txt详细配置");
            List<Co_TxtAnalyze> co_TxtAnalyzs = new ArrayList<Co_TxtAnalyze>();
            String txtAnalyzeSql = "select * from co_txtanalyze where dstype = '" + dataSetCode + "' and dsflag = '" + bmly + "' order by refieldseq";
            logger.info("txtAnalyzeSql-->" + txtAnalyzeSql);
            JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            co_TxtAnalyzs = jdbcTemplate.query(txtAnalyzeSql, new BeanPropertyRowMapper<Co_TxtAnalyze>(Co_TxtAnalyze.class));
            logger.info("查询Co_TxtAnalyze解析配置表：" + co_TxtAnalyzs.size());

            int flagNum = 0;// 计数
            int nameNum = 0;// 数据项的数量
            int dataNum = 0;// 数值的行数
            boolean Nflag = true;// 进入数据项解析的标志
            boolean Dflag = true;// 进入数据值解析的标志
            boolean Xflag = true;// 处理
            int startRow = 0;

            String endLine = null;
            String zg_flag = pvo.getReceiveCode();// 账管标识
            //查询TXT截取总长度
            logger.info("开始截取txt总长度配置");
            List<Co_TxtAnalyze> co_TxtAnalyzsList = new ArrayList<Co_TxtAnalyze>();
            String co_TxtAnalyzsListSql = "select * from co_txtanalyze where dstype = '" + dataSetCode + "' and dsflag = '" + bmly + "' order by refieldseq";
            jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            co_TxtAnalyzsList = jdbcTemplate.query(co_TxtAnalyzsListSql, new BeanPropertyRowMapper<Co_TxtAnalyze>(Co_TxtAnalyze.class));
            logger.info("开始获取长度校验条件");
            logger.info("数据长度校验查询条件：" + dataSetCode + "标识：" + zg_flag);
            String co_Trancodeconfig_TmpCriteriaSql = "select * from co_trancodeconfig_tmp where GlRBM = '" + zg_flag + "' and bmly = '" + bmly + "' and dataset = '" + dataSetCode + "' and driectionflag = 'snd'";
            logger.info("长度校验条件sql-->" + co_Trancodeconfig_TmpCriteriaSql);
            jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
            List<Co_Trancodeconfig_Tmp> tranCodeConfigList = jdbcTemplate.query(co_Trancodeconfig_TmpCriteriaSql, new BeanPropertyRowMapper<Co_Trancodeconfig_Tmp>(Co_Trancodeconfig_Tmp.class));
            Integer fieldLength = null;
            logger.info("查询接口数据长度校验配置表：" + tranCodeConfigList.size());
            if (tranCodeConfigList.size() > 0) {
                fieldLength = Integer.parseInt(tranCodeConfigList.get(0).getC_1());
                logger.info("数据长度校验配置表长度为" + fieldLength);
            } else {
                logger.error("未查到数据长度校验配置表长度");
            }
            // 存储对外表数据的集合
            List<Co_ToExData> exList = new ArrayList<Co_ToExData>();
            while ((lineValue = br.readLine()) != null) {
                flagNum++;
                if (lineValue.length() > 0) {
                    if (flagNum == 1) {
                        if (!errorFileName.split("_")[5].equals(lineValue.trim())) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值与数据文件的序号不同！";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 2) {
                        if (!"TXT".equalsIgnoreCase(lineValue.trim())) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求!";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 3) {
                        if (!"V1.0".equalsIgnoreCase(lineValue.trim())) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求!";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 4) {
                        if (StringUtils.isEmpty(lineValue)) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求!";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 5) {
                        if (StringUtils.isEmpty(lineValue)) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求!";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 6) {
                        if (!pvo.getFileName().split("\\.")[0].split("_")[2].equals(lineValue.trim())) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行日期与压缩包日期不一致！";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 7) {
                        if (StringUtils.isEmpty(lineValue)) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求！";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 8) {
                        if (StringUtils.isEmpty(lineValue)) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求！";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    if (flagNum == 9) {
                        if (!StringUtils.isEmpty(lineValue) && !dataSetCode.equals(lineValue.trim())) {
                            String errorInfo = errorFileName + "_文件第" + flagNum + "行的值不符合要求！";
                            logger.error("txt文件中errorInfo-->" + errorInfo);
                            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                        }
                    }
                    // 在此判断数据项的每一项，数据长度，非空字段判断
                    //由于前headNum行为表头部分， 则headNum+1行为数据项数量的行
                    if (headNum + 1 == flagNum && Nflag) {
                        nameNum = Integer.parseInt(lineValue);
                        Nflag = false;
                        startRow = headNum + 1;// startRow=10
                    }
                    /**
                     * 校验字段数与字段值
                     */
                    if (startRow + nameNum == flagNum && !Nflag && Xflag) {
                        String str = co_TxtAnalyzs.get(co_TxtAnalyzs.size() - 1).getRefield();
                        if (!str.equalsIgnoreCase(lineValue.trim())) {
                            String errorInfo = "文件字段名不符合规范";
                            while ((lineValue = br.readLine()) != null) {
                                // 获得最后一行数据值
                                endLine = lineValue;
                            }
                            return endLine;
                        }
                    }
                    // 至此（startRow+1+nameNum）为数据值数量的行
                    try {
                        if (startRow + nameNum + 1 == flagNum && !Nflag && Xflag) {
                            dataNum = Integer.parseInt(lineValue);
                            startRow = startRow + 1 + nameNum;
                            Xflag = false;
                            Dflag = false;
                        }
                    } catch (NumberFormatException e) {
                        logger.error("数据格式转换异常" + e.getMessage());
                    }
                    // 下面读取的是数据值的行，并对读取的行数据进行截取
                    try {
                        if (flagNum > startRow && flagNum <= startRow + dataNum && !Dflag) {
                            // 统计该数据行中文字的个数
                            logger.info("数据行总长-->" + dataNum);
                            //检验数据长度
                            logger.info("开始校验数据行的长度-->");
                            checkDataLength(bmly, fieldLength, lineValue, charset, dataSetCode, flagNum, errorFileName);
                            logger.info("开始进行数据解析-->");
                            // 解析数据开始
                            Co_ToExData vCo_ToExData = new Co_ToExData();
                            processFieldSet(lineValue, vCo_ToExData, dataSetCode, bmly, errorFileName, flagNum, pvo, co_TxtAnalyzsList, charset);// dsFlag需修改
                            //对外表数据填入集合
                            exList.add(vCo_ToExData);
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        String errorInfo = errorFileName + "_导入文件的记录数与recordNumber对应的数字不一致，请检查导入文件!";
                        logger.error("errorInfo-->" + errorInfo);
                        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                    }
                    // 校验数据中有空行的情况
                } else {
                    String errorInfo = errorFileName + "_文件第" + flagNum + "行数据为空，请检查导入文件!";
                    logger.error("errorInfo-->" + errorInfo);
                    fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
                }
                // 获得最后一行数据值
                endLine = lineValue;
            }
            // 数据校验END
            if (!endLine.equals("END") || "".equals(endLine) || endLine == null) {
                String errorInfo = errorFileName + "_文件中最后一行未找到END结束符，请检查导入文件!";
                logger.error("errorInfo-->" + errorInfo);
                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
            }
            //生成excel文件
            genExcelRowAndCell(fileName, exList, co_TxtAnalyzs);
            exList.clear();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return dataSetCode;
    }

    //数据解析
    private void processFieldSet(String lineValue, Co_ToExData co_ToExData, String dsType, String dsFlag,
                                 String errorFileName, int flagNum, ParaTransportVO pvo, List<Co_TxtAnalyze> co_TxtAnalyzs, String charset) throws Exception {
        // 初始化标志位
        int startAdrs = 0;
        for (int i = 0; i < co_TxtAnalyzs.size(); i++) {
            Co_TxtAnalyze mCo_TxtAnalyze = new Co_TxtAnalyze();
            mCo_TxtAnalyze = co_TxtAnalyzs.get(i);

            // 取小数点后的长度
            int dLength = 0;
            // 判断字段类型是否为数值类型
            if (mCo_TxtAnalyze.getRefieldtype().equals("N")) {
                // 处理整数的数值类型
                if (mCo_TxtAnalyze.getRefieldlength().contains(",") == false) {
                    dLength = 0;
                } else {
                    dLength = Integer.parseInt(mCo_TxtAnalyze.getRefieldlength().split("\\,")[1]);
                }
            }
            // 在此加入异常处理信息，处理数据类型转换错误
            try {
                Object object = getFieldValue(lineValue, mCo_TxtAnalyze, errorFileName, flagNum, pvo, startAdrs, charset);
                String startAdrs1 = object.toString().split(",")[1];

                startAdrs = Integer.parseInt(startAdrs1);
                object = object.toString().split(",")[0];

                // 为字段赋值
                saveField(co_ToExData, mCo_TxtAnalyze.getTypecode(), object, mCo_TxtAnalyze.getRefieldtype(), dLength);
            } catch (NumberFormatException e) {
                logger.error(e.toString());
                String errorInfo = errorFileName + "_文件" + flagNum + "行字段名" + mCo_TxtAnalyze.getRefield() + "数据类型转换错误!";
                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
            }
        }
    }

    private void saveField(Object obj, String method, Object inObj, String inFormat, int dLength) throws Exception {
        // 处理字段为数值类型并且为空的情况
        if ((inFormat.toUpperCase()).equals("N") && (inObj == null || inObj.equals(""))) {
            // 字段为数值类型并且值为空时,赋值为0
            inObj = "0";
        }
        int flag_int = 0;
        if (!(inFormat == null || inFormat.trim().equals(""))) {
            if ((inFormat.toUpperCase()).equals("N")) {
                flag_int = 1;
            }
        }
        Method methodObj = (Method) methodMap.get(new StringBuffer(20).append("set").append(method).toString().toUpperCase());
        if (methodObj != null) {
            try {
                if (flag_int == 1) {
                    // 判断小数点后的位数（2位或是4位,6位）
                    if (dLength == 0) {
                        inObj = String.valueOf((Long.parseLong(inObj.toString()) / 1));
                    } else {//不支持小数长度为1的情况
                        inObj = numberFormat(dLength, inObj);
                    }
                    flag_int = 0;
                } else if (flag_int == 4) {
                    SimpleDateFormat df = new SimpleDateFormat(inFormat);
                    inObj = df.parse(inObj.toString());
                    flag_int = 0;
                }
                methodObj.invoke(obj, inObj);
            } catch (Exception e) {
                logger.error(e.toString());
                throw e;
            }
        }

    }

    //检查数据长度
    private void checkDataLength(String flag, int fieldLength, String lineValue, String charset,
                                 String dataSetCode, int flagNum, String errorFileName) {
        try {
            if ((lineValue.getBytes(charset).length) != fieldLength) {
                logger.info("接口：" + dataSetCode + " 标识：" + flag + " 数据库长度: " + fieldLength + " txt中长度：" + (lineValue.getBytes(charset).length));
                String errorInfo = errorFileName + "_文件第" + flagNum + "行中数据字节长度不对！";
                logger.error("errorInfo-->" + errorInfo);
                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    //获取数据值
    private Object getFieldValue(String lineValue, Co_TxtAnalyze co_TxtAnalyze, String errFileName, int flagNum,
                                 ParaTransportVO pvo, int startAdrs, String charset) throws SQLException {
        // 获取读取字段长度
        int fLength = Integer.parseInt(co_TxtAnalyze.getRefieldlength().split("\\,")[0]);
        int fieldLength = startAdrs + fLength;
        String reslutValue = subByteString(lineValue, startAdrs, fieldLength, charset).trim();
        startAdrs = startAdrs + fLength;
        if ("".equals(reslutValue) || reslutValue == null) {
            if ("Y".equals(co_TxtAnalyze.getC1())) {
                String errorInfo = errFileName + "_文件" + flagNum + "行字段名" + co_TxtAnalyze.getRefield() + "为必输项,但未找到具体的值!!很可能之前的字段长度不对";
                logger.error("errorInfo-->" + errorInfo);
                fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + errorInfo + "\n");
            }
        }
        return reslutValue + "," + startAdrs;
    }

    //字符串拆分
    private String subByteString(String src, int beginIndex, int endIndex, String charset) {
        byte[] b;
        String reslutV = "";
        try {
            b = src.getBytes(charset);
            byte[] nb;
            int len_src = src.length();
            int len_byte = b.length;
            if (len_src == len_byte) {
                reslutV = src.substring(beginIndex, endIndex);
            } else {
                int i = 0;
                int len = endIndex - beginIndex;
                nb = new byte[len];
                for (; beginIndex < endIndex; beginIndex++) {
                    nb[i] = b[beginIndex];
                    i++;
                }
                reslutV = new String(nb, 0, nb.length, charset);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("解析数据时,编码集错误-->" + e.toString());
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "解析数据时,编码集错误" + "\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("解析数据时,数组下标越界-->" + e.toString());
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "解析数据时,数组下标越界" + "\n");
        }
        return reslutV;
    }

    //处理带小数点的金额类数据，避免科学计数法的问题
    private Object numberFormat(int length, Object obj) {
        if (Double.parseDouble(obj.toString()) == 0) {
            obj = String.valueOf(((double) Long.parseLong(obj.toString()) / 100));
        } else {
            long pow = (long) Math.pow(10, length);
            String str = String.valueOf(pow).replace("1", "#.");
            DecimalFormat df = new DecimalFormat(str);
            BigDecimal bf = new BigDecimal(obj.toString());
            obj = df.format(bf.divide(new BigDecimal(pow)));
        }
        if (obj.toString().substring(0, 1).equals(".")) {
            obj = "0" + obj;
        }
        return obj;
    }


    //横向生成excel
    private boolean genExcelRowAndCell(String filename, List<Co_ToExData> co_ToExDatas, List<Co_TxtAnalyze> co_txtAnalyzes) {
        logger.info("开始生成txt数据解析结果Excel-->");
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "开始生成txt数据解析结果Excel-->" + "\n");
        filename = filename.split("\\.")[0] + ".xls";
        String excelFilePath = userHomePath + File.separator + filename;
        logger.info("excel路径为-->" + excelFilePath);
        fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "excel路径为-->" + excelFilePath + "\n");
        File file = new File(excelFilePath);
        FileOutputStream fileOutputStream = null;
        SXSSFWorkbook sxssfWorkbook = null;
        try {
            sxssfWorkbook = new SXSSFWorkbook(100);
            sxssfWorkbook.createSheet();
            SXSSFSheet sheet = sxssfWorkbook.getSheetAt(0);
            //标题字体
            Font titleFont = sxssfWorkbook.createFont();
            titleFont.setFontName("黑体");
            titleFont.setFontHeightInPoints((short) 12);
            titleFont.setColor(Font.COLOR_RED);//红色

            CellStyle titleCellStyle = sxssfWorkbook.createCellStyle();
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);//横向居中
            titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//竖向居中
            titleCellStyle.setFont(titleFont);

            //正文字体
            Font font = sxssfWorkbook.createFont();
            font.setFontName("黑体");
            font.setFontHeightInPoints((short) 12);
            font.setColor(Font.COLOR_NORMAL);//黑色

            CellStyle cellStyle = sxssfWorkbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);//横向居中
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//竖向居中
            cellStyle.setFont(font);

            int rowNum = 0;//行
            int colNum = 0;//列
            //标题行(列名)
            Row titleRow = (Row) sheet.createRow(rowNum);
            Cell cell = null;
            for (int i = 0; i < co_txtAnalyzes.size(); i++) {
                sheet.setColumnWidth(i, new Integer(co_txtAnalyzes.get(i).getRefieldlength().split(",")[0])
                        > co_txtAnalyzes.get(i).getC2().length() * 2
                        ? new Integer(co_txtAnalyzes.get(i).getRefieldlength().split(",")[0]) * 300
                        : co_txtAnalyzes.get(i).getC2().length() * 2 * 275);
                cell = titleRow.createCell(i);
                cell.setCellStyle(titleCellStyle);
                cell.setCellValue(co_txtAnalyzes.get(i).getRefield());
            }
            rowNum++;
            cell = null;
            //（汉字意思）
            Row chinarow = (Row) sheet.createRow(rowNum);
            for (colNum = 0; colNum < co_txtAnalyzes.size(); colNum++) {
                cell = chinarow.createCell(colNum);
                cell.setCellType(CellType.STRING);
                cell.setCellStyle(titleCellStyle);
                cell.setCellValue(co_txtAnalyzes.get(colNum).getC2());
            }
            rowNum++;

            //副行（字段长度）
            Row row = (Row) sheet.createRow(rowNum);
            cell = null;
            for (colNum = 0; colNum < co_txtAnalyzes.size(); colNum++) {
                cell = row.createCell(colNum);
                cell.setCellType(CellType.STRING);
                cell.setCellStyle(titleCellStyle);
                cell.setCellValue(co_txtAnalyzes.get(colNum).getRefieldlength());
            }
            rowNum++;

            for (; rowNum < co_ToExDatas.size() + 3; rowNum++) {
                row = sheet.createRow(rowNum);
                Co_ToExData co_toExData = co_ToExDatas.get(rowNum - 3);

                for (int i = 0; i < co_txtAnalyzes.size(); i++) {
                    cell = row.createCell(i);
                    cell.setCellType(CellType.STRING);
                    cell.setCellStyle(cellStyle);
                    Co_TxtAnalyze co_txtAnalyze = co_txtAnalyzes.get(i);
                    String typecode = co_txtAnalyze.getTypecode();
                    String object = (String) getObject(co_toExData, typecode);
                    cell.setCellValue(object);
                }
            }
            fileOutputStream = new FileOutputStream(file);
            sxssfWorkbook.write(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            logger.info("Excel-->" + filename + "生成完毕");
            fileAnaylizeService.appendText("[" + DateUtil.now() + "] " + "Excel-->" + filename + "生成完毕" + "\n");
            Runtime runtime = null;
            try {
                runtime = Runtime.getRuntime();
                if (!SystemUtil.getOsInfo().isWindows()) {
                    // System.out.println("is linux");
                    runtime.exec("nautilus " + userHomePath);
                } else {
                    runtime.exec("cmd /c start explorer " + userHomePath);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (null != runtime) {
                    runtime.runFinalization();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
        } finally {
            co_ToExDatas.clear();
            co_ToExDatas = null;
            try {
                sxssfWorkbook.close();
                sxssfWorkbook = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {

                }
            }
        }
        return false;
    }


    private Object getObject(Co_ToExData co_toExData, String method) throws Exception {
        Method methodObj = (Method) txtMap.get(new StringBuffer(20).append("get").append(method).toString().toUpperCase());
        Object invoke = methodObj.invoke(co_toExData);
        return invoke;
    }
}
