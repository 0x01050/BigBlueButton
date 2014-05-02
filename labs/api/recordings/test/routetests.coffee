#assert      = require('assert')
chai        = require('chai')
hapi        = require('hapi')

routes      = require('../lib/routes')

assert      = chai.assert

# integration tests for API endpoint


# setup server with firing up - use inject instead
server = new hapi.Server()
server.route(routes.routes)


# parseurls endpoint test
describe 'add endpoint', ->

  it 'add - should add two numbers together', ->
    server.inject({method: 'PUT', url: '/sum/add/5/5'}, (res) ->
        assert.deepEqual({'equals': 10}, JSON.parse(res.payload))
        done()
    )

  it 'add - should error if a string is passed', (done) ->
    server.inject({method: 'PUT', url: '/sum/add/100/x'}, (res) ->
        assert.deepEqual({
          'statusCode': 400,
          'error': 'Bad Request',
          'message': 'the value of b must be a number',
          'validation': {
            'source': 'path',
            'keys': [
              'b'
            ]
          }
        }, JSON.parse(res.payload))
        done()
    )