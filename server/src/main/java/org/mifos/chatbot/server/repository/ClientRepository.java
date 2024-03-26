package org.mifos.chatbot.server.repository;

import org.mifos.chatbot.server.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;


public interface ClientRepository extends JpaRepository<Client, Integer> {

}
