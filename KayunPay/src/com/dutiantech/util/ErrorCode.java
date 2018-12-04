package com.dutiantech.util;

public enum ErrorCode {
	REQ_ROLE {
		public String val() {return "REQ_ROLE";}
		public String desc() {return "角色请求";}
	},
	REQ_ROLES {
		public String val() {return "REQ_ROLES";}
		public String desc() {return "多角色请求";}
	},
	REQ_PERM {
		public String val() {return "REQ_PERM";}
		public String desc() {return "许可请求";}
	},
	REQ_PERMS {
		public String val() {return "REQ_PERMS";}
		public String desc() {return "多许可请求";}
	},
	REQ_LOGIN {
		public String val() {return "REQ_LOGIN";}
		public String desc() {return "登录请求";}
	},
	REQ_GUEST {
		public String val() {return "REQ_GUEST";}
		public String desc() {return "访客请求";}
	};
	
	public abstract String val();
	public abstract String desc();
}
