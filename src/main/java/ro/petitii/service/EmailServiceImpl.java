package ro.petitii.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ro.petitii.model.Email;
import ro.petitii.repository.EmailRepository;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Override
    public Email save(Email e) {
        return emailRepository.save(e);
    }

    @Override
    public long count() {
        return emailRepository.count();
    }

    @Override
    public long lastUid() {
        PageRequest pr = new PageRequest(0,1, Sort.Direction.DESC, "uid");
        Page<Email> result = emailRepository.findAll(pr);
        if (result.getSize()>0) return result.getContent().get(0).getUid();
        else return -1;
    }
}
