package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class OPUserV2 extends Model< OPUserV2 >{

	private static final long serialVersionUID = -3157880689785009810L;
	
	public static final OPUserV2 OPUserV2Dao = new OPUserV2();
	
	public enum USER{
		OPCODE{
			public String key(){
				return "OPUSER_CODE_" ;
			}
		},
		OPMAP{
			public String key(){
				return "OPUSER_MAP_";
			}
		}
//		,
//		ROLEMAP{
//			public String key(){
//				return "ROLE_MAP_";
//			}
//		}
		;
		public abstract String key();
	}
}
