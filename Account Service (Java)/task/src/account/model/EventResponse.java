package account.model;

public record EventResponse (
        String date,
        String action,
        String subject,
        String object,
        String path
){
}
