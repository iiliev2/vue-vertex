var packageJSON = require('./package.json');
var path = require('path');
var webpack = require('webpack');

const PATHS = {
    build: path.join(__dirname, "..", "vw-web", 'target', 'classes',
        'WEB-INF')
};

module.exports = {
    entry: ['./src/main/js/index.js'],

    devServer: {
        inline: true,
        port: 23003
    },

    output: {
        path: PATHS.build,
        publicPath: '/',
        filename: path.join('jsbundles', 'app-bundle.js')
    },
    module: {
        rules: [
            // process *.vue files using vue-loader
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            // process *.js files using babel-loader
            // the exclude pattern is important so that we don't
            // apply babel transform to all the dependencies!
            {
                test: /\.js$/,
                exclude: /(node_modules|bower_components)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015'],
                        plugins: ['transform-runtime']
                    }
                }
            }]
    },
    plugins: [
        //needed because we cannot run in -p mode, because we cannot
        //use uglifyjs as it does not support es6
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify('production')
        }),
        //needed for axios as es5 does not have the promise api
        new webpack.ProvidePlugin({
            Promise: 'es6-promise-promise'
        })
    ]
};
