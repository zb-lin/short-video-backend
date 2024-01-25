package com.lzb.shortvideo.service;

import com.lzb.shortvideo.model.entity.User;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;

@SpringBootTest
public class MongoDBTest {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserService userService;

    @Test
    public void saveUser() {
        User user = userService.getById(1717188659762479105L);
        mongoTemplate.save(user);
    }

    public User findUserByUserName(String userName) {
        Query query = new Query(Criteria.where("userName").is(userName));
        User user = mongoTemplate.findOne(query, User.class);
        return user;
    }


    /**
     * 更新对象
     * 可以选择更新⼀条数据，或者更新多条数据
     *
     * @param user
     * @return
     */
    public long updateUser(User user) {
        Query query = new Query(Criteria.where("id").is(user.getId()));
        Update update = new Update().set("userName", user.getUserName()).set("passWord"
                , user.getUserPassword());
        //更新查询返回结果集的第⼀条
        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);
        //更新查询返回结果集的所有
        // mongoTemplate.updateMulti(query,update,UserEntity.class);
        if (result != null)
            return result.getMatchedCount();
        else
            return 0;
    }

    /**
     * 删除对象
     *
     * @param id
     */
    public void deleteUserById(Long id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, User.class);
    }
}
