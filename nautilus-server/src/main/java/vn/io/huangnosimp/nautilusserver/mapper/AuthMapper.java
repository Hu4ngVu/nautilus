package vn.io.huangnosimp.nautilusserver.mapper;

import org.mapstruct.Mapper;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.models.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    User toUser(RegisterUser registerUser);
}
