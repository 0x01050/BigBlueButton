#
# the idea is that this way we prevent test runs (for whenever needed)

# Change to start meteor in production or development mode
ENVIRONMENT_TYPE=development

JASMINE_SERVER_UNIT=0 JASMINE_SERVER_INTEGRATION=0 JASMINE_CLIENT_INTEGRATION=0 JASMINE_BROWSER=PhantomJS JASMINE_MIRROR_PORT=3000 ROOT_URL=http://127.0.0.1/html5client NODE_ENV=$ENVIRONMENT_TYPE meteor

