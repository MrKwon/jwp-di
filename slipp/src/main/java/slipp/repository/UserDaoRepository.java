package slipp.repository;

import nextstep.stereotype.Repository;
import slipp.dao.UserDao;
import slipp.domain.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoRepository implements UserRepository {

    private UserDao userDao = UserDao.getInstance();

    public void insert(User user) {
        userDao.insert(user);
    }

    public Optional<User> findById(String userId) {
        return Optional.of(userDao.findByUserId(userId));
    }

    public void update(User user) {
        userDao.update(user);
    }

    public List<User> findAll() throws SQLException {
        return userDao.findAll();
    }
}
