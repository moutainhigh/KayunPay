package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.Funds;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.vo.VipV2;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class UserService extends BaseService {
	
	private static final String basic_selectFields = "uid,userMobile,userCode,userScore,userName,userEmail,userState,regDate,regTime,regIP,lastLoginDateTime,lastLoginIp,loginCount,sysDesc,vipLevel,vipLevelName,vipInterestRate,vipRiskRate,loginId,userType,inviteCode,evaluationLevel,jxAccountId";
	
	public String getMobile(String userCode){
		try {
			String zhazha = Db.queryStr("select userMobile from t_user where userCode = ?",userCode);
			zhazha = CommonUtil.decryptUserMobile(zhazha);
			if(CommonUtil.isMobile(zhazha)){
				return zhazha;
			}else{
				return "";
			}
		} catch (Exception e) {
			return "";
		}
		
	}
	
	public User findByMobile(String uMobile){
		String enMobile = "" ;
		try{
			enMobile = CommonUtil.encryptUserMobile(uMobile);
		}catch(Exception e){
			e.printStackTrace( );
			return null ;
		}
		String querySql = "select u.userCode,u.userName,u.userEmail,u.userState,u.loginId,u.userType,u.inviteCode,u.jxAccountId,"
				+ "i.userCardName,i.userCardId,i.userAdress,b.bankNo,b.mobile "
				+ "from t_user u left join t_user_info i on u.userCode=i.userCode "
				+ " left join t_banks_v2 b on u.userCode = b.userCode "
				+ " where u.userMobile=?";
		User user = User.userDao.findFirst(querySql, enMobile) ;
		if( user == null )
			return null ;
		user.put("userMobile", uMobile ) ;
		
		String userCardId = user.getStr("userCardId");
		try{
			userCardId = CommonUtil.decryptUserCardId(userCardId) ;
		}catch(Exception e){
			userCardId = " ";
		}
		user.put("userCardId", userCardId ) ;
		 
		return user ;
	}
	
	/**
	 * 根据ID查询一个用户
	 * @param userCode
	 * @return
	 */
	public User findById(String userCode){
		User user = User.userDao.findById(userCode);
		return user;
	}
	
	/**
	 * 根据userCode查询可选字段 WJW
	 * @param userCode
	 * @param fields
	 * @return
	 */
	public User findByFields(String userCode,String...fields){
		String sqlSelect = "select ";
		String sqlData = "";
		String sqlFrom = " from t_user where userCode=?";
		for (String str : fields) {
			sqlData += StringUtil.isBlank(sqlData) ? str:(","+str);
		}
		return User.userDao.findFirst(sqlSelect+sqlData+sqlFrom,userCode);
	}
	
	/**
	 * 查询指定字段
	 * @return
	 */
	public User findByField(String userCode,String field){
		User user = User.userDao.findByIdLoadColumns(userCode,field);
		return user;
	}
	
	/**
	 * 查询一个用户的待收本息
	 * @param userCode
	 * @return
	 */
	public long findBeRecyMoney(String userCode){
		return Db.queryLong("select beRecyPrincipal + beRecyInterest from t_funds where userCode = ?",userCode);
	}
	
	/**
	 * 5月活动升级   
	 * @param userCode		用户编码
	 * @param loanCode		查询用户当前这个标的投标金额(因为当前的投标流水还不是还款中的状态)，所以需要30000元减去这个当前投标金额
	 */
	public void _51ShengJiHuoDong(String userCode,String loanCode){
		User user = findById(userCode) ;
		long xxx = Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where payUserCode = ? and loanCode = ?",userCode,loanCode).longValue();
		String nowDate = DateUtil.getNowDate();
		if(DateUtil.compareDateByStr("yyyyMMdd", nowDate, "20160501") > -1 && DateUtil.compareDateByStr("yyyyMMdd", nowDate, "20160531") < 1){
			boolean ok_vip4 = validateTouBiaoJinE("20160501000000","20160531235959",3000000-xxx,userCode);
			if(ok_vip4 && user.getInt("vipLevel") < 4){
				VipV2 vip_51 = VipV2.getVipByLevel(4);
				user.set("vipLevel", vip_51.getVipLevel() ) ;
				user.set("vipLevelName", vip_51.getVipLevelName() ) ;
				user.set("vipInterestRate", vip_51.getVipInterestRate() ) ;
				user.set("vipRiskRate", vip_51.getVipRiskRate() ) ;
				user.update();
			}
		}
	}
	
	/**
	 * 	更新积分，冗余更新等级操作
	 * @param userCode
	 * @param score
	 * @return
	 */
	public User updateScore(String userCode , long score ){
		User user = findById(userCode) ;
		long userScore = user.getLong("userScore");
		long nowScore = userScore + score ;
		
		VipV2 vip = VipV2.getVipByValue(nowScore) ;
		user.set("userScore", nowScore ) ;
		if(user.getInt("vipLevel") < vip.getVipLevel()){
			if(vip.getVipLevel()==6){
				long amount =findBeRecyMoney(userCode);
				if(amount>=10000000){
					user.set("vipLevel", vip.getVipLevel() ) ;
					user.set("vipLevelName", vip.getVipLevelName() ) ;
					user.set("vipInterestRate", vip.getVipInterestRate() ) ;
					user.set("vipRiskRate", vip.getVipRiskRate() ) ;
				}
			}else if(vip.getVipLevel()==7){
				long amount =findBeRecyMoney(userCode);
				if(amount>=20000000){
					user.set("vipLevel", vip.getVipLevel() ) ;
					user.set("vipLevelName", vip.getVipLevelName() ) ;
					user.set("vipInterestRate", vip.getVipInterestRate() ) ;
					user.set("vipRiskRate", vip.getVipRiskRate() ) ;
				}
			}else if(vip.getVipLevel() >= 1 && vip.getVipLevel() <= 5){
				user.set("vipLevel", vip.getVipLevel() ) ;
				user.set("vipLevelName", vip.getVipLevelName() ) ;
				user.set("vipInterestRate", vip.getVipInterestRate() ) ;
				user.set("vipRiskRate", vip.getVipRiskRate() ) ;
			}
		}
		//用户当前活跃积分如果低于当前VIP等级的最低活跃积分，则补积分到会员等级对应的最低活跃积分
		long nowUserVipMinScore = CommonUtil.yunsuan(VipV2.getVipByLevel(user.getInt("vipLevel")).getVipMinScore()+"", "100", "cheng", 0).longValue();
		if(user.getLong("userScore") < nowUserVipMinScore){
			user.set("userScore", nowUserVipMinScore);
		}
		if( user.update() ){
			return user ;
		}
		return null ;
	}
	
	public boolean validateTouBiaoJinE(String startDateTime,String endDateTime,long payAmount,String userCode){
		long sum_payAmount = Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where loanDateTime >= ? and loanDateTime <= ? and payUserCode = ? and loanState in ('N','O','P','Q')",startDateTime,endDateTime,userCode).longValue();
//		long sum_payAmount = Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",userCode).longValue();
		if(sum_payAmount>=payAmount){
			return true;
		}
		return false;
	}
	
	/**
	 * 根据userCode查询用户的全部信息
	 * @param userCode
	 * @return
	 */
	public User findUserAllInfoById(String userCode){
		String t_user_selectFields = "t1.userCode,t1.userName,t1.userMobile,t1.userEmail,t1.userState,t1.regDate,t1.regTime,t1.regIP,t1.lastLoginDateTime,t1.lastLoginIp,t1.loginCount,t1.sysDesc,t1.vipLevel,t1.vipLevelName,t1.vipInterestRate,t1.vipRiskRate,t1.userScore,t1.loginId,t1.beforeVip,t1.jxAccountId ";
		String t_user_info_selectFields = "t2.userCardName,t2.userCardId,t2.isAuthed,t2.userAdress,t2.ecpNme1,t2.ecpRlation1,t2.ecpMbile1,t2.ecpNme2,t2.ecpRlation2,t2.ecpMbile2,t2.cardImg,t2.idType";
		StringBuffer sqlStr = new StringBuffer("select");
		sqlStr.append(" ").append(t_user_selectFields).append(",").append(t_user_info_selectFields);
		sqlStr.append(" from t_user t1 left join t_user_info t2 on t1.userCode = t2.userCode where t1.userCode = ?");
		User user = User.userDao.findFirst(sqlStr.toString(), userCode);
		return user;
	}
	
	/**
	 * 根据昵称查询用户的主要信息(用户编码、电话、邮箱、真实姓名、身份证)
	 * @param userCode
	 * @return
	 */
	public User findUserAllInfoByUserName(String userName){
		String t_user_selectFields = "t1.userCode,t1.userName,t1.userMobile,t1.userEmail";
		String t_user_info_selectFields = "t2.userCardName,t2.userCardId";
		StringBuffer sqlStr = new StringBuffer("select");
		sqlStr.append(" ").append(t_user_selectFields).append(",").append(t_user_info_selectFields);
		sqlStr.append(" from t_user t1 left join t_user_info t2 on t1.userCode = t2.userCode where t1.userCode = ?");
		User user = User.userDao.findFirst(sqlStr.toString(), userName);
		return user;
	}
	
	/**
	 * 通过 loginAuthCode 获取用户信息
	 * @param loginAuthCode
	 * @return
	 */
	public User find4AuthCode(String loginAuthCode){
		List<User> listUser = User.userDao.find("select " + basic_selectFields + " from t_user where loginAuthCode = ?"
				, loginAuthCode);
		return listUser == null || listUser.size() <= 0 ? null : listUser.get(0);
	}
	
	/**
	 * 通过 mobile 获取用户信息
	 * @param mobile
	 * @return
	 */
	public User find4mobile(String mobile){
		String userMobile;
		try {
			userMobile = CommonUtil.encryptUserMobile(mobile);
		} catch (Exception e) {
			return null;
		}
		List<User> listUser = User.userDao.find("select " + basic_selectFields + " from t_user where userMobile = ?"
				,userMobile);
		return listUser == null || listUser.size() <= 0 ? null : listUser.get(0);
	}
	
	/**
	 * 通过 用户姓名 获取用户信息
	 * @param mobile
	 * @return
	 */
	public List<User> find4userName(String userName){
		List<User> listUser = User.userDao.find("select " + basic_selectFields + " from t_user where userName = ?"
				,userName.trim());
		return listUser;
	}
	
	/**
	 * 验证用户状态是否为正常
	 * @param userCode
	 * @return   true 用户状态【正常】
	 */
	public boolean validateUserState(String userCode){
		String userState = User.userDao.findByIdLoadColumns(userCode, "userState").getStr("userState");
		if(userState.equals(SysEnum.userState.N.val()))
			return true;
		return false;
	}
	
	public Page<User> findInfo4scoreByPage(Integer pageNumber, Integer pageSize, String allkey){
		String sqlSelect = "select t1.userCode,t1.userName,t1.vipLevelName,t1.userScore,t2.points,t3.userCardName ";
		String sqlFrom = "from t_user t1 left join t_funds t2 on t1.userCode=t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"t1.userName","t1.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t1.userMobile", "=" , x , "or");
		}
		
		sqlFrom = sqlFrom+ makeSql4Where(buff) ;
		String sqlOrder = " order by t2.updateDateTime desc,t1.uid desc";
		return User.userDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlOrder , ps.toArray()) ;
	}
	
	
	/**
	 * * 分页查询用户
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @param beginDate		开始日期(注册日期)
	 * @param endDate		结束日期(注册日期)
	 * @param userStatus	用户状态
	 * @return
	 */
	public Page<User> findByPage(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String userState, String allkey){
		
		String sqlSelect = "select t1.*,t2.userCardName, t3.points ";
		String sqlFrom = " FROM t_user t1 LEFT JOIN t_user_info t2 ON t1.userCode = t2.userCode LEFT JOIN t_funds t3 ON t1.userCode = t3.userCode";
		String sqlOrder = " order by t1.uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"t1.userName","t1.userEmail","t2.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		
		makeExp(buff, ps, "t1.regDate", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.regDate", "<=" , endDate , "and");
		makeExp(buff, ps, "t1.userState", "=" , userState , "and");
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t1.userMobile", "=" , x , "or");
		}
		return User.userDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	
	/**
	 * * 查询认证进行中的用户列表
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @param beginDate		开始日期(注册日期)
	 * @param endDate		结束日期(注册日期)
	 * @param userStatus	用户状态
	 * @return
	 */
	public Page<User> findAuthedByPage(Integer pageNumber, Integer pageSize, String allkey){
		
		String sqlSelect = "select t2.userCardName,t2.isAuthed,t1.userCode,t1.userName,t1.userState,t1.lastLoginDateTime,t1.vipLevelName ";
		String sqlFrom = " from t_user t1 left join t_user_info t2 on t1.userCode = t2.userCode where t1.userState='N' and t2.isAuthed in('1','3') ";
		String sqlOrder = " order by t1.uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"t1.userName","t1.userEmail","t2.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t1.userMobile", "=" , x , "or");
		}
		return User.userDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4WhereHasWhere(buff)+sqlOrder , ps.toArray()) ;
	}
	
	/**
	 * 添加一个用户
	 * @param userMobile	手机号码
	 * @param userEmail		邮箱
	 * @param loginPasswd	密码
	 * @param userName		昵称
	 * @param regIP			注册IP
	 * @param sysDesc		备注
	 * @param tyj			是否送投标体验金
	 * @return
	 */
	public boolean save(String userCode,String userMobile, String userEmail, String loginPasswd, String userName, String regIP,String sysDesc){
		String authcode = "";
		String passwd = "";
		String uMobile = "";
		//邀请码
		String inviteCode = "";
		do {
			inviteCode = CommonUtil.getVerifiCode(6);
		} while (findByInviteCode(inviteCode) != null);
		try {
			passwd = CommonUtil.encryptPasswd(loginPasswd);
			authcode = CommonUtil.buildLoginAuthCode(userMobile, loginPasswd);
			uMobile = CommonUtil.encryptUserMobile(userMobile);
		} catch (Exception e) {
			return false;
		}
		User user = new User();
		user.set("userCode", userCode);
		user.set("userName", userName);
		user.set("userMobile", uMobile);
		user.set("userEmail", userEmail);
		user.set("loginPasswd", passwd);
		user.set("loginAuthCode", authcode);
		user.set("userState", "N");
		user.set("regDate", DateUtil.getNowDate());
		user.set("regTime", DateUtil.getNowTime());
		user.set("regIP", regIP);
		user.set("lastLoginDateTime", DateUtil.getNowDateTime());
		user.set("lastLoginIp",regIP);
		user.set("loginCount", 0);
		user.set("sysDesc", sysDesc);
		VipV2 vip = VipV2.getVipByLevel(1);
		user.set("vipLevel", vip.getVipLevel());
		user.set("vipLevelName", vip.getVipLevelName());
		user.set("vipInterestRate", vip.getVipInterestRate());
		user.set("vipRiskRate", vip.getVipRiskRate());
		user.set("userScore", 0);
		user.set("inviteCode", inviteCode);
		if(user.save()){
			if(initUserInfo(userCode) && initFunds(userCode,userName)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 添加一个用户
	 * @param userMobile	手机号码
	 * @param userEmail		邮箱
	 * @param loginPasswd	密码
	 * @param userName		昵称
	 * @param regIP			注册IP
	 * @param sysDesc		备注
	 * @param tyj			是否送投标体验金
	 * @return
	 */
	public boolean save(String userCode,String userMobile, String userEmail, String loginPasswd, String userName, String regIP,String sysDesc,String jxAccountId){
		String authcode = "";
		String passwd = "";
		String uMobile = "";
		//邀请码
		String inviteCode = "";
		do {
			inviteCode = CommonUtil.getVerifiCode(6);
		} while (findByInviteCode(inviteCode) != null);
		try {
			passwd = CommonUtil.encryptPasswd(loginPasswd);
			authcode = CommonUtil.buildLoginAuthCode(userMobile, loginPasswd);
			uMobile = CommonUtil.encryptUserMobile(userMobile);
		} catch (Exception e) {
			return false;
		}
		User user = new User();
		user.set("userCode", userCode);
		user.set("userName", userName);
		user.set("userMobile", uMobile);
		user.set("userEmail", userEmail);
		user.set("loginPasswd", passwd);
		user.set("loginAuthCode", authcode);
		user.set("userState", "N");
		user.set("regDate", DateUtil.getNowDate());
		user.set("regTime", DateUtil.getNowTime());
		user.set("regIP", regIP);
		user.set("lastLoginDateTime", DateUtil.getNowDateTime());
		user.set("lastLoginIp",regIP);
		user.set("loginCount", 0);
		user.set("sysDesc", sysDesc);
		user.set("jxAccountId", jxAccountId);
		VipV2 vip = VipV2.getVipByLevel(1);
		user.set("vipLevel", vip.getVipLevel());
		user.set("vipLevelName", vip.getVipLevelName());
		user.set("vipInterestRate", vip.getVipInterestRate());
		user.set("vipRiskRate", vip.getVipRiskRate());
		user.set("userScore", 0);
		user.set("inviteCode", inviteCode);
		if(user.save()){
			if(initUserInfo(userCode) && initFunds(userCode,userName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 验证用户是否存在(待优化 资金账户部分)
	 * @param userCode
	 * @return  1 用户存在，用户认证信息不存在；2用户不存在，用户认证信息存在；0都存在；-1其它
	 */
	public int isExist(String userCode){
		long x = Db.queryLong("select count(userCode) from t_user where userCode = ?", userCode);
		long y = Db.queryLong("select count(userCode) from t_user_info where userCode = ?", userCode);
		long z = Db.queryLong("select count(userCode) from t_funds where userCode = ?", userCode);
		if(x > 0 && y < 1){
			return 1;
		}else if(x < 1 && y > 0){
			return 2;
		}else if(x>0 && y>0 && z>0){
			return 0;
		}
		return -1;
	}
	
	/**
	 * 更新用户状态	N正常  P人工冻结    S系统冻结
	 * @param idValue	userCode
	 * @param userState	用户状态
	 * @return
	 */
	public boolean updateUserState(String userCode, String userState){
		User user = User.userDao.findByIdLoadColumns(userCode,"userCode,userState");
		user.set("userState", userState);
		return user.update();
	}
	
	/**
	 * 修改用户基本信息
	 * @param userCode			用户编码
	 * @param newUserName		昵称,null不修改
	 * @param newPassword		明文新密码,null不修改
	 * @param newEmail			新邮箱,null不修改
	 * @return
	 */
	public boolean updateUser(String userCode, String newUserName,String newPassword, String newEmail){
		try {
			User user = User.userDao.findByIdLoadColumns(userCode,"userCode,userName,userMobile,loginPasswd,loginAuthCode,userEmail");
			if(!StringUtil.isBlank(newUserName)){
				user.set("userName", newUserName);
			}
			if(!StringUtil.isBlank(newPassword)){
				String userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
				String authcode = CommonUtil.buildLoginAuthCode(userMobile, newPassword);
				String passwd = CommonUtil.encryptPasswd(newPassword);
				user.set("loginPasswd", passwd);
				user.set("loginAuthCode",authcode);
			}
			if(!StringUtil.isBlank(newEmail)){
				user.set("userEmail", newEmail);
			}
			return user.update();
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 用户登录后修改相关信息
	 * @param userCode		用户标识
	 * @param requestIP		请求IP
	 * @param loginCount	当前登录次数
	 * @return
	 */
	public int updateUser4Login(String userCode, String requestIP) {
		return Db.update("update t_user set lastLoginDateTime = ?, lastLoginIp = ?, loginCount = loginCount + 1 where userCode = ?"
				, DateUtil.getNowDateTime(),requestIP,userCode);
	}
	
	
	/**
	 * 重置用户密码
	 * @param userCode	用户编号
	 * @param newPasswd	新密码-明文
	 * @return
	 */
	public boolean resetPasswd(String userCode, String newPasswd){
		try {
			User user = User.userDao.findByIdLoadColumns(userCode,"userCode,userMobile,loginPasswd,loginAuthCode");
			String userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			String authcode = CommonUtil.buildLoginAuthCode(userMobile,newPasswd);
			String passwd = CommonUtil.encryptPasswd(newPasswd);
			user.set("loginPasswd", passwd);
			user.set("loginAuthCode",authcode);
			return user.update();
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean modifyUserScore(String userCode , String opType , int score){
		String updateSql = "update t_user set userScore = userScore " + opType + " ? where userCode=?";
		Object[] ps = new Object[2];
		ps[0] = score ;
		ps[1] = userCode ;
		return Db.update(updateSql, ps ) == 1 ;
	}
	
	/**
	 * 根据 ID删除用户(同时删除资金、用户认证信息)
	 * @param idValue userCode
	 * @return
	 */
//	@Deprecated
//	public boolean deleteById(String userCode){
//		Funds.fundsDao.deleteById(userCode);
//		UserInfo.userInfoDao.deleteById(userCode);
//		return User.userDao.deleteById(userCode);
//	}
	
	/**
	 * 初始化用户认证信息
	 * @param userCode
	 * @return
	 */
	private boolean initUserInfo(String userCode){
		UserInfo userInfo = new UserInfo();
		userInfo.set("userCode", userCode);
		userInfo.set("userCardName", "");
		userInfo.set("idType", "01");
		userInfo.set("userCardId", "f2af8ce442e3c757");
		userInfo.set("cardImg", "");
		userInfo.set("isAuthed", "0");
		userInfo.set("userAdress", "");
		userInfo.set("ecpNme1", "");
		userInfo.set("ecpRlation1", "");
		userInfo.set("ecpMbile1", "");
		userInfo.set("ecpNme2", "");
		userInfo.set("ecpRlation2", "");
		userInfo.set("ecpMbile2", "");
		userInfo.set("userExtInfo", "");
		userInfo.set("userInfoMac", "");
		return userInfo.save();
	}
	
	/**
	 * 初始化资金账户
	 * @param userCode	用户编码
	 * @return
	 */
	private boolean initFunds(String userCode, String userName){
		Funds funds = new Funds();
		funds.set("userCode", userCode);
		funds.set("userName", userName);
		funds.set("avBalance", 0);
		funds.set("frozeBalance", 0);
		funds.set("beRecyCount", 0);
		funds.set("beRecyPrincipal", 0);
		funds.set("beRecyInterest", 0);
		funds.set("reciedPrincipal", 0);
		funds.set("reciedInterest", 0);
		funds.set("points", 0);
		funds.set("loanTotal", 0);
		funds.set("loanCount", 0);
		funds.set("loanSuccessCount", 0);
		funds.set("loanBySysCount", 0);
		funds.set("beRecyPrincipal4loan", 0);
		funds.set("beRecyInterest4loan", 0);
		funds.set("beRecyMFee4loan", 0);
		funds.set("updateDate", DateUtil.getNowDate());
		funds.set("updateTime", DateUtil.getNowTime());
		funds.set("fundsMac", "");
		funds.set("rewardRateAmount", 0);
		return funds.save();
	}

	/**
	 * 通过邮箱查询用户
	 * @param string
	 * @return
	 */
	public User findByEmail(String email) {
		return User.userDao.findFirst("select * from t_user where userEmail = ?", email);
	}
	
	/**
	 * 根据用户等级查询统计用户数量
	 * @param startLevel	最小等级
	 * @param endLevel		最大等级,如果小于1，就查询等级等于最小等级的
	 * @return
	 */
	public long countRank(int startLevel, int endLevel){
		if(endLevel<1){
			return Db.queryLong("select count(t1.userCode) from t_user t1 inner join t_funds t2 on t1.userCode = t2.userCode where t1.vipLevel = ? and t2.loanTotal<=0",startLevel);
		}
		return Db.queryLong("select count(t1.userCode) from t_user t1 inner join t_funds t2 on t1.userCode = t2.userCode where t1.vipLevel >= ? and t1.vipLevel <= ? and t2.loanTotal<=0",startLevel,endLevel);
	}

	/**
	 * 修改手机号码
	 * @param userCode		用户昵称
	 * @param newMobile		新手机号码
	 * @param newPassword	新初始化密码
	 * @return
	 */
	public boolean updateUserMobile(String userCode,String newMobile, String newPassword) {
		try {
			String passwd = CommonUtil.encryptPasswd(newPassword);
			String authcode = CommonUtil.buildLoginAuthCode(newMobile, newPassword);
			String mobile = CommonUtil.encryptUserMobile(newMobile);
			User user = new User();
			user.set("userCode", userCode);
			user.set("userMobile", mobile);
			user.set("loginPasswd", passwd);
			user.set("loginAuthCode", authcode);
			return user.update();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 验证用户是否开通存管账户
	 * @param user
	 * @return	true - 已开通
	 * 			false - 未开通
	 */
	public boolean isFuiouAccount(String userCode) {
		String loginId = Db.queryStr("select loginId from t_user where userCode = ?", userCode);
		return !StringUtil.isBlank(loginId);
	}
	
	/**
	 * 根据userCode获取用户loginId  20170822  WCF
	 * @throws Exception 
	 */
	public String getLoginId(String userCode) throws Exception {
		User user = User.userDao.findById(userCode);
		if (!(user == null)) {
			String loginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
			return loginId;
		} else {
			return null;
		}		
	}
	
	public List<User> findAllInvestor() {
		return User.userDao.find("SELECT " + basic_selectFields + " FROM t_user WHERE isnull(userType) AND userCode NOT IN (SELECT loanUserCode FROM t_loan_apply GROUP BY loanUserCode)");
	}
	
	/**
	 * 根据 inviteCode 获取用户信息
	 * @param inviteCode
	 * @return
	 */
	public User findByInviteCode(String inviteCode){
		List<User> listUser = User.userDao.find("SELECT " + basic_selectFields + " FROM t_user WHERE inviteCode = ?", 
				inviteCode);
		return listUser.size() <= 0 ? null : listUser.get(0);
	}  
	
	/*
	 * 根据userCode更新surveyResult  20180127
	 */
	public boolean updateSurvey(String userCode,String result){
		
		String sql = "update t_user set evaluationLevel=? where userCode=?";
		int num = Db.update(sql, result,userCode);
		
		if(num>=0){
			return true;
		}else{
			return false;
		}
		
		
	}
	
	/**
	 * 根据存管电子账户查询用户信息
	 * @param jxAccountId   存管电子账号
	 * @return
	 */
	public User findByJXAccountId(String jxAccountId){
		User user = User.userDao.findFirst("SELECT " + basic_selectFields + " FROM t_user WHERE jxAccountId = ?", jxAccountId);
		return user;
	}
	
	/**
	 * * 分页查询已绑卡用户
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @return
	 */
	public Page<User> findByPage(Integer pageNumber, Integer pageSize){
		
		String sqlSelect = "select t1.*,t2.trueName,t2.cardid,t2.bankNo ";
		String sqlFrom = " from t_user t1 left join t_banks_v2 t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.regDate desc";
		
		return User.userDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder ) ;
	}
	
	/**
	 * 更新用户标识
	 */
	public Boolean updateUserType(String userCode ,String userType){
		
		String sql = "update t_user set userType =? where userCode =? ";
        int num = Db.update(sql, userType,userCode);
		
		if(num>0){
			return true;
		}else{
			return false;
		}
	}

}