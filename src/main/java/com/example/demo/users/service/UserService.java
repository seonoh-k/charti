package com.example.demo.users.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.users.exception.UserNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO entityToDTO(Users users) {

        if (users == null) return null;

        return UserDTO.builder()
                .id(users.getId())
                .username(users.getUsername())
                .uuid(users.getUuid())
                .password(users.getPassword())
                .name(users.getName())
                .role(users.getRole().name())
                .phoneNumber(users.getPhoneNumber())
                .provider(users.getProvider())
                .providerId(users.getProviderId())
                .build();
    }
    public Users dtoToEntity(UserDTO dto) {

        if (dto == null) return null;

        return Users.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .name(dto.getName())
                .uuid(dto.getUuid())
                .password(dto.getPassword())
                .role(Role.valueOf(dto.getRole()))
                .phoneNumber(dto.getPhoneNumber())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .build();
    }


    /**
     * 멤버를 생성하고 Enum 타입의 객체를 반환한다.
     *
     * <br/>반환값 참고 -> {@link UserStatus}
     *
     * @param userDTO : 저장할 UserDTO 객체
     * @return UserStatus : 상태와 메세지를 담고있다.
     *
     */
    @Transactional
    public UserStatus createMember(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());
        if(byUsername.isEmpty()){
            // Join Logic
            String rawPassword= userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            userDTO.setPassword(encodedPassword);
            userDTO.setRole(Role.ROLE_MEMBER.name());
            userDTO.setPhoneNumber(userDTO.getPhoneNumber());

            userRepository.save(this.dtoToEntity(userDTO));


            return UserStatus.JOIN_SUCCESS;
        } else{
            return UserStatus.JOIN_FAIL;
        }
    }
    /**
     * 소셜로그인 멤버를 생성하고 Enum 타입의 객체를 반환한다.
     *
     * <br/>반환값 참고 -> {@link UserStatus}
     *
     * @param userDTO : 저장할 UserDTO 객체
     * @return UserStatus : 상태와 메세지를 담고있다.
     *
     */
    @Transactional
    public UserStatus createSocialMember(UserDTO userDTO) {
        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());
        if(byUsername.isEmpty()){
            // Join Logic
            userDTO.setRole(Role.ROLE_MEMBER.name());

            userRepository.save(this.dtoToEntity(userDTO));


            return UserStatus.JOIN_SUCCESS;
        } else{
            return UserStatus.JOIN_FAIL;
        }
    }

    @Transactional
    public UserDTO changeRoleToAdmin(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_ADMIN);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToManager(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_MANAGER);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToExpert(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_EXPERT);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }
    @Transactional
    public UserDTO changeRoleToMember(UserDTO userDTO){

        Optional<Users> byUsername = userRepository.findByUsername(userDTO.getUsername());

        if(byUsername.isEmpty()){
            throw new UserNotFoundException("MEMBER NOT FOUND");
        } else{
            Users users = byUsername.get();
            users.setRole(Role.ROLE_MEMBER);
            Users saved = userRepository.save(users);
            return this.entityToDTO(saved);
        }
    }


    /**
     * 이메일로 유저가 데이터베이스에 존재하는지 확인한다.
     *
     * @param email : 확인할 이메일
     * @return true : 존재함, false : 존재하지 않음
     *
     */
    public boolean existsByEmail(String email){
        return userRepository.existsByUsername(email);
    }

    /**
     * 전화번호로 유저가 데이터베이스에 존재하는지 확인한다.
     *
     * @param phoneNumber : 확인할 전화번호
     * @return true : 존재함, false : 존재하지 않음
     */
    public boolean existsByPhoneNumber(String phoneNumber){
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     *
     * @param provider
     * @param providerId
     * @return
     */
    public Optional<Users> findByProviderAndProviderId(String provider, String providerId){
        return userRepository.findByProviderAndProviderId(provider,providerId);
    }

    /**
     * 이메일로 멤버를 가져온다.
     *
     * @param email : 확인할 이메일
     * @return UserDTO: Member Entity객체를 변환하여 UserDTO객체로 반환한다.
     * @throws UserNotFoundException 해당하는 유저가 없는 경우 발생
     */
    public UserDTO getMemberByEmail(String email){
        Optional<Users> byUsername = userRepository.findByUsername(email);
        if (byUsername.isPresent()){
            return this.entityToDTO(byUsername.get());
        } else{
            throw new UserNotFoundException("유저가 없어요");
        }
    }

    /**
     * UUID로 유저를 가져온다.
     *
     * @param uuid : 확인할 uuid
     * @return UserDTO : Member Entity객체를 변환하여 UserDTO객체로 반환한다.
     * @throws UserNotFoundException 해당하는 유저가 없는 경우 발생
     */
    public UserDTO getMemberByUUID(String uuid){
        Optional<Users> byUuid = userRepository.findByUuid(uuid);
        if(byUuid.isPresent()){
            return this.entityToDTO(byUuid.get());
        } else{
            throw new UserNotFoundException("유저가 없어요");
        }
    }
}
