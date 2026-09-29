package vn.io.huangnosimp.nautilusserver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.io.huangnosimp.nautilusserver.dto.request.RegisterUser;
import vn.io.huangnosimp.nautilusserver.models.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "password")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "serverCount", constant = "0")
    @Mapping(target = "collectionCount", constant = "0")
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    User toUser(RegisterUser registerUser);
}
