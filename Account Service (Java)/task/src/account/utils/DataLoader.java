package account.utils;

import account.entity.Group;
import account.entity.Role;
import account.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final GroupRepository groupRepository;

    @Autowired
    public DataLoader(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
        createRoles();
    }

    private void createRoles() {
        try {
            System.out.println("Saving roles and groups...");
            groupRepository.save(new Group(Role.ADMINISTRATOR));
            groupRepository.save(new Group(Role.USER));
            groupRepository.save(new Group(Role.ACCOUNTANT));
            groupRepository.save(new Group(Role.AUDITOR));
        } catch (Exception ignored) {
        }
    }
}
