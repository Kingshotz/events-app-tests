package ge.eventsapp.db;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
    @Select("SELECT enabled FROM usr WHERE id = #{id}")
    Boolean isUserEnabled(@Param("id") Long id);
}
