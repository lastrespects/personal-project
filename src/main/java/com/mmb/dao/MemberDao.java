// MemberDao.java
package com.mmb.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.mmb.dto.Member;

@Mapper
public interface MemberDao {

	@Select("""
			SELECT *
				FROM `member`
				WHERE loginId = #{loginId}
			""")
	Member getMemberByLoginId(String loginId);
    
    // Member DTO의 확장 필드 (dailyTarget 등)를 모두 포함하도록 수정
    @Select("""
			SELECT *
				FROM `member`
				WHERE id = #{id}
			""")
	Member getMemberById(int id);

	@Insert("""
			INSERT INTO `member`
			    SET regDate = NOW()
			        , updateDate = NOW()
			        , loginId = #{loginId}
			        , loginPw = #{loginPw}
			        , `name` = #{name}
			""")
	void joinMember(String loginId, String loginPw, String name);

	@Select("""
			SELECT loginId
				FROM `member`
				WHERE id = #{id}
			""")
	String getLoginId(int id);
	
}