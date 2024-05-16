package midas.SoundOfFlower.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import static midas.SoundOfFlower.entity.QUser.user;

public class UserRepositoryImpl implements DeleteUser {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void deleteByEmailWhereIsNull() {

        queryFactory
                .delete(user)
                .where(user.email.isNull())
                .execute();

        em.flush();
        em.clear();
    }
}
