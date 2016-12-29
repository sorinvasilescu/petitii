package ro.petitii.service.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ro.petitii.model.EmailTemplate;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mpostelnicu on 12/29/2016.
 */
@Service
public class EmailTemplateProcessorService {

  @Autowired
  protected TemplateEngine templateEngine;

    /**
     * Extract the variable names found within ${...} part of the template content
     *
     * @param template
     * @return a set with variable names
     */
  public Set<String> extractVariables(EmailTemplate template) {
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(template.getContent());
      Set<String> ret=new TreeSet<>();
      while(matcher.find()) {
          ret.add(matcher.group(1));
      }
      return ret;
  }


    /**
     * Processes an {@link EmailTemplate} with the given id and replacing the variables with the values
     * provided in variables parameter
     * @param templateId the Id of the {@link EmailTemplate} to load from the database
     * @param variables a map with variable names and values to replace during processing
     * @return the processed template
     */
  public String processTemplateWithId(Long templateId, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariables(variables);
    return templateEngine.process(EmailTemplateResolver.PREFIX+templateId.toString(), context);
  }
}