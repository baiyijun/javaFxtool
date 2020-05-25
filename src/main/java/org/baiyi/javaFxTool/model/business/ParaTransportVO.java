package org.baiyi.javaFxTool.model.business;
import java.util.HashMap;

public class ParaTransportVO {
	private String monType;//监听类型  01、主动监听；02、被动监听
	private String filePath;//文件路径
	private String fileName;//文件名称	
	private String receiveCode;//接收机构代码(乙方机构号)
	private String sendCode;//发送机构代码（甲方机构号）	
	private String taskDirection;//传输方向（乙方到甲方、甲方到乙方）
	private String isInOrg;//是否是机构内收发	
	private String busiDataID;//业务监听数据编号
	private String sendType;//发送方式	
	private String senddate;//发送日期 (yyyyMMDD)
	private String sendDate;//发送日期 (yyyyMMDD)
	private String monDataType;//监听数据类型
	
	private String fileBusiType;//收发文件业务类型(主任务方法)
	
	private String priorityLevel;//任务优先级
	private String fileRename;//重新命名的文件名称   //工行托管的需要重新命名
	private String dataSetCode;//主数据集代码	
	private String packetId;	//包名称
	
	private String ourPartyCode;//我方机构代码
	private String otherPartyCode;//对方机构代码

	private String transType;//交易类别
	private HashMap<String,Object> paramHashMap;
	private String workFlowNo;
	public String getMonType() {
		return monType;
	}
	public void setMonType(String monType) {
		this.monType = monType;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getReceiveCode() {
		return receiveCode;
	}
	public void setReceiveCode(String receiveCode) {
		this.receiveCode = receiveCode;
	}
	public String getSendCode() {
		return sendCode;
	}
	public void setSendCode(String sendCode) {
		this.sendCode = sendCode;
	}
	public String getTaskDirection() {
		return taskDirection;
	}
	public void setTaskDirection(String taskDirection) {
		this.taskDirection = taskDirection;
	}
	public String getIsInOrg() {
		return isInOrg;
	}
	public void setIsInOrg(String isInOrg) {
		this.isInOrg = isInOrg;
	}
	public String getBusiDataID() {
		return busiDataID;
	}
	public void setBusiDataID(String busiDataID) {
		this.busiDataID = busiDataID;
	}
	public String getSendType() {
		return sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}
	public String getSenddate() {
		return senddate;
	}
	public void setSenddate(String senddate) {
		this.senddate = senddate;
	}
	public String getSendDate() {
		return sendDate;
	}
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}
	public String getMonDataType() {
		return monDataType;
	}
	public void setMonDataType(String monDataType) {
		this.monDataType = monDataType;
	}
	public String getFileBusiType() {
		return fileBusiType;
	}
	public void setFileBusiType(String fileBusiType) {
		this.fileBusiType = fileBusiType;
	}
	public String getPriorityLevel() {
		return priorityLevel;
	}
	public void setPriorityLevel(String priorityLevel) {
		this.priorityLevel = priorityLevel;
	}
	public String getFileRename() {
		return fileRename;
	}
	public void setFileRename(String fileRename) {
		this.fileRename = fileRename;
	}
	public String getDataSetCode() {
		return dataSetCode;
	}
	public void setDataSetCode(String dataSetCode) {
		this.dataSetCode = dataSetCode;
	}
	public String getPacketId() {
		return packetId;
	}
	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}
	public String getOurPartyCode() {
		return ourPartyCode;
	}
	public void setOurPartyCode(String ourPartyCode) {
		this.ourPartyCode = ourPartyCode;
	}
	public String getOtherPartyCode() {
		return otherPartyCode;
	}
	public void setOtherPartyCode(String otherPartyCode) {
		this.otherPartyCode = otherPartyCode;
	}
	public String getTransType() {
		return transType;
	}
	public void setTransType(String transType) {
		this.transType = transType;
	}
	public HashMap<String, Object> getParamHashMap() {
		return paramHashMap;
	}
	public void setParamHashMap(HashMap<String, Object> paramHashMap) {
		this.paramHashMap = paramHashMap;
	}
	public String getWorkFlowNo() {
		return workFlowNo;
	}
	public void setWorkFlowNo(String workFlowNo) {
		this.workFlowNo = workFlowNo;
	}
	
	
}
