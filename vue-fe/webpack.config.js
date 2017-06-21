var packageJSON = require('./package.json');
var path = require('path');
var webpack = require('webpack');

const PATHS = {
	build : path.join(__dirname, "..", "vw-web", 'target', 'classes',
			'META-INF', 'resources', 'webjars', packageJSON.name,
			packageJSON.version)
};

module.exports = {
	entry : './app/index.js',

	devServer : {
		inline : true,
		port : 23001
	},

	output : {
		path : PATHS.build,
		publicPath : '/tmp/',
		filename : 'app-bundle.js'
	}

};
