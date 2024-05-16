package midas.SoundOfFlower.repository.user;

import midas.SoundOfFlower.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, DeleteUser {

    boolean existsBySocialId(String socialId);

    Optional<User> findByNickName(String nickname);

    Optional<User> findBySocialTypeAndEmail(String socialType,String email);

    boolean existsByEmailAndSocialType(String email,String socialType);
    Optional<User> findBySocialId(String socialId);

    boolean existsByNickName(String nickName);
}
