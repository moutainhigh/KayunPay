package com.dutiantech.controller.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class ExportFileController extends BaseController {

	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);

	@ActionKey("/exportExcel4User")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void exportExcel4User() {
		Message msg = new Message();
		try {
			String beginDate = getPara("beginDate");
			String endDate = getPara("endDate");
			String output_html = "";
			String output_extType = "text/html";
			String filename = "tmpExcel.xls";
			String act = getPara("type");
			HttpServletResponse response = getResponse();
			// 验证时间范围
			if (StringUtil.isBlank(beginDate) || StringUtil.isBlank(endDate)) {
				msg = error("01", "开始时间、结束时间不能为空", null);
				renderJson(msg);
				return;
			}
			if (beginDate.length() != 8 || endDate.length() != 8) {
				msg = error("02", "开始时间、结束时间不合法！", null);
				renderJson(msg);
				return;
			}
			long x = CommonUtil.compareDateTime(endDate, beginDate, "yyyyMMdd");
			if (x > 100) {
				msg = error("03", "最大时间范围不可超过100天，当前范围：" + x + "天！", null);
				renderJson(msg);
				return;
			}
			
			if (StringUtil.isBlank(act)) {
				msg = succ("success", null);
				renderJson(msg);
				return;
			} else if ("download".equals(act)) {
				response.setCharacterEncoding("utf-8");
				output_extType = "application/vnd.ms-excel";
				// 生成THML格式表单，导出Excel
				try {
					String bb = getPara("bb", "");
					if (bb.equals("funds1")) {
						filename = "理财人账户";
						// output_html = funds1(beginDate, endDate);
					} else if (bb.equals("funds2")) {
						filename = "借款人账户";
						// output_html = funds2(beginDate, endDate);
					} else if (bb.equals("funds-tx")) {
						filename = "提现流水";
						// output_html = funds_tx(beginDate, endDate);
					} else if (bb.equals("funds-cz")) {
						filename = "充值流水";
						// output_html = funds_cz(beginDate, endDate);
					} else if (bb.equals("funds-trace")) {
						filename = "资金流水";
						// output_html = funds_trace(beginDate, endDate);
					} else if (bb.equals("caiwuzhuanyong1")) {
						filename = "月还款统计";
						// output_html = caiwuzhuanyong1(beginDate, endDate);
					} else if (bb.equals("funds-detail")) {
						filename = "资金明细";
						output_html = funds_detail(beginDate, endDate);
					}
				} catch (Exception e) {
					filename = "导出异常:" + filename + DateUtil.getNowDateTime() + ".xls";
				}
				filename = new String(filename.getBytes("utf-8"), "ISO_8859_1");
				response.setHeader("Content-Disposition", "attachment;filename=" + filename + "_"
						+ beginDate + "-" + endDate + ".xls");
				renderText(output_html, output_extType);
				return;
			} else {
				msg = error("05", "请求异常", null);
				renderJson(msg);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = error("04", "导出Excel表时异常", null);
			renderJson(msg);
			return;
		}
	}

	/**
	 * 资金明细
	 * 
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String funds_detail(String beginDate, String endDate) {
		String traceType = getPara("traceType");
		String fundsType = "";// getPara("fundsType");

		String userCode = getUserCode();

		int pageNumber = 1;
		int pageSize = 4500;
		String output_html = "<table border='1'>";
		output_html += "<tr><td colspan='8' style='text-align:center;'><b>资金明细</b></td></tr>";
		output_html += "<tr><td><b>时间</b></td><td><b>类型</b></td><td><b>收入</b></td><td><b>支出</b></td><td><b>手续费</b></td><td><b>可用金额</b></td><td><b>冻结金额</b></td><td><b>备注</b></td></tr>";

		Map<String, Object> result_firstPage = fundsTraceService.findByPage4NoobExcel(pageNumber, pageSize, beginDate,
				endDate, traceType, fundsType, userCode);
		ArrayList<FundsTrace> fundsTraces = (ArrayList<FundsTrace>) result_firstPage.get("list");
		int totalRow = (int) result_firstPage.get("totalRow");
		if (totalRow > 0) {
			output_html += funds_detail_makeHtml((List<FundsTrace>) fundsTraces);
			long sumTraceAmount = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType, null, userCode);
			output_html += "<tr><td colspan='8'>总计交易金额:" + Number.longToString(sumTraceAmount) + "元</td></tr>";
		}
		output_html += "</table>";
		return output_html;
	}

	private String funds_detail_makeHtml(List<FundsTrace> list) {

		String output_html = "";
		for (int i = 0; i < list.size(); i++) {
			output_html += "<tr>";
			FundsTrace tmp = list.get(i);

			String traceDateTime = tmp.getStr("traceDateTime");
			if (!StringUtil.isBlank(traceDateTime)) {
				traceDateTime = DateUtil.parseDateTime(DateUtil.getDateFromString(traceDateTime, "yyyyMMddHHmmss"),
						"yyyy-MM-dd HH:mm:ss");
			} else {
				traceDateTime = " ";
			}
			output_html += "<td>" + traceDateTime + "</td>";
			String traceType = tmp.getStr("traceType");
			// 根据SysEnum枚举类，取出traceType字母对应文字叙述
			traceType = SysEnum.traceType.valueOf(traceType).desc();
			output_html += "<td>" + traceType + "</td>";
			if (tmp.getStr("fundsType").equals("J")) {
				output_html += "<td>" + getMoneyStr(tmp.getLong("traceAmount").toString()) + "元</td>";
				output_html += "<td></td>";
			} else {
				output_html += "<td></td>";
				output_html += "<td>" + getMoneyStr(tmp.getLong("traceAmount").toString()) + "元</td>";
			}
			output_html += "<td>" + getMoneyStr(tmp.getInt("traceFee").toString()) + "元</td>";
			output_html += "<td>" + getMoneyStr(tmp.getLong("traceBalance").toString()) + "元</td>";
			output_html += "<td>" + getMoneyStr(tmp.getLong("traceFrozeBalance").toString()) + "元</td>";
			output_html += "<td>" + tmp.getStr("traceRemark") + "</td>";
			output_html += "</tr>";
		}
		return output_html;
	}

	/**
	 * 分子钱转元单位
	 * 
	 * @param fenziqian
	 * @return
	 */
	private String getMoneyStr(String fenziqian) {
		if (fenziqian.length() == 2) {
			return "0." + fenziqian;
		}
		if (fenziqian.length() == 1) {
			return "0.0" + fenziqian;
		}
		String tmp1 = fenziqian.substring(0, fenziqian.length() - 2);
		String tmp2 = fenziqian.substring(fenziqian.length() - 2, fenziqian.length());
		return tmp1 + "." + tmp2;
	}
}
