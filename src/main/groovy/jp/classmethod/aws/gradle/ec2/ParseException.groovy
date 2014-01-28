package jp.classmethod.aws.gradle.ec2


class ParseException extends Exception {
	
	String expression
	
	ParseException(String expression) {
		this.expression = expression
	}
	
	@Override
	public String getMessage() {
		return "fail to parse expression: $expression";
	}
}
