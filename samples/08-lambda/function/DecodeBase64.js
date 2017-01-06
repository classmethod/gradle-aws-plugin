console.log('Loading function');

exports.handler = function(event, context) {
    console.log(JSON.stringify(event, null, 2));
    var payload = new Buffer(event.data, 'base64').toString(process.env.TARGET);
    context.succeed(payload);
};
