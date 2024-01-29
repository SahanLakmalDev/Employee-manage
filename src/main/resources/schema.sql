CREATE TABLE IF NOT EXISTS employee(
    emp_id VARCHAR(30) PRIMARY KEY ,
    name VARCHAR(100) NOT NULL ,
    contact VARCHAR(30) NOT NULL ,
    address VARCHAR(200) NOT NULL ,
    status BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE IF NOT EXISTS salary (
    salary_id INT PRIMARY KEY AUTO_INCREMENT,
    year VARCHAR(100) NOT NULL ,
    month VARCHAR(100) NOT NULL ,
    salary DECIMAL(10,2) NOT NULL ,
    create_date_time TIMESTAMP NOT NULL ,
    emp_id VARCHAR(30),
    CONSTRAINT fk_emp FOREIGN KEY (emp_id) REFERENCES employee(emp_id)
);
