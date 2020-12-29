db2 connect to zodak
OPTS="modified by coldel0x09 striplzeros codepage=1208"
db2 export to MORTGAGE.MORTGAGE_APPLICANT.del of del $OPTS "select * from MORTGAGE.MORTGAGE_APPLICANT"
db2 export to MORTGAGE.MORTGAGE_CUSTOMER.del of del $OPTS "select * from MORTGAGE.MORTGAGE_CUSTOMER"
db2 export to MORTGAGE.MORTGAGE_DEFAULT.del of del $OPTS "select * from MORTGAGE.MORTGAGE_DEFAULT"
db2 export to MORTGAGE.MORTGAGE_PROPERTY.del of del $OPTS "select * from MORTGAGE.MORTGAGE_PROPERTY"
